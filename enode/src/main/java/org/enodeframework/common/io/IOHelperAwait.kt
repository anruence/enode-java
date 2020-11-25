package org.enodeframework.common.io

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asCompletableFuture
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.function.Action1
import org.enodeframework.common.function.Action2
import org.enodeframework.common.function.DelayedTask
import org.enodeframework.common.function.Func
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

/**
 * @author anruence@gmail.com
 */
object IOHelperAwait {

    private val logger = LoggerFactory.getLogger(IOHelperAwait::class.java)

    @JvmStatic
    fun <TAsyncResult> tryAsyncActionRecursively(
            asyncActionName: String,
            asyncAction: Func<Deferred<TAsyncResult>>,
            successAction: Action1<TAsyncResult>,
            getContextInfoFunc: Func<String?>,
            failedAction: Action2<Throwable, String>?,
            retryTimes: Int,
            retryWhenFailed: Boolean) {
        return tryAsyncActionRecursively(asyncActionName, asyncAction, successAction, getContextInfoFunc, failedAction, retryTimes, retryWhenFailed, 3, 1000)
    }

    @JvmStatic
    fun <TAsyncResult> tryAsyncActionRecursively(
            asyncActionName: String,
            asyncAction: Func<Deferred<TAsyncResult>>,
            successAction: Action1<TAsyncResult>,
            getContextInfoFunc: Func<String?>,
            failedAction: Action2<Throwable, String>?,
            retryTimes: Int,
            retryWhenFailed: Boolean,
            maxRetryTimes: Int,
            retryInterval: Int) {
        val asyncTaskExecutionContext = AsyncTaskExecutionContext(asyncActionName, asyncAction, successAction, getContextInfoFunc, failedAction, retryTimes, retryWhenFailed, maxRetryTimes, retryInterval)
        return asyncTaskExecutionContext.execute()
    }

    @JvmStatic
    fun <TAsyncResult> tryAsyncActionRecursivelyWithoutResult(
            asyncActionName: String,
            asyncAction: Func<Deferred<TAsyncResult>>,
            successAction: Action1<TAsyncResult>,
            getContextInfoFunc: Func<String?>,
            failedAction: Action2<Throwable, String>?,
            retryTimes: Int,
            retryWhenFailed: Boolean) {
        val asyncTaskExecutionContext = AsyncTaskExecutionContext(asyncActionName, asyncAction, successAction, getContextInfoFunc, failedAction, retryTimes, retryWhenFailed, 3, 1000)
        asyncTaskExecutionContext.execute()
    }

    @JvmStatic
    fun <T> tryIOFuncAsync(func: Func<Deferred<T>>, funcName: String): CompletableFuture<T> {
        return try {
            func.apply().asCompletableFuture()
        } catch (ex: Exception) {
            throw IORuntimeException(String.format("%s failed.", funcName), ex)
        }
    }

    internal class AsyncTaskExecutionContext<TAsyncResult>(private val actionName: String, asyncAction: Func<Deferred<TAsyncResult>>, successAction: Action1<TAsyncResult>, contextInfoFunc: Func<String?>, failedAction: Action2<Throwable, String>?, retryTimes: Int, retryWhenFailed: Boolean, maxRetryTimes: Int, retryInterval: Int) {
        private val asyncAction: Func<Deferred<TAsyncResult>>
        private val successAction: Action1<TAsyncResult>
        private val contextInfoFunc: Func<String?>
        private val failedAction: Action2<Throwable, String>?
        private var currentRetryTimes: Int
        private val retryWhenFailed: Boolean
        private val maxRetryTimes: Int
        private val retryInterval: Int
        fun execute() {
            var asyncResult = CompletableFuture<TAsyncResult>()
            try {
                asyncResult = asyncAction.apply().asCompletableFuture()
            } catch (ex: Exception) {
                asyncResult.completeExceptionally(ex)
            }
            if (asyncResult.isCancelled) {
                asyncResult.exceptionally { ex: Throwable ->
                    logger.error("Task '{}' was cancelled, contextInfo: {}, current retryTimes: {}.",
                            actionName,
                            getContextInfo(contextInfoFunc),
                            currentRetryTimes, ex)
                    executeFailedAction(ex, String.format("Task '%s' was cancelled.", actionName))
                    null
                }
            }
            asyncResult
                    .thenAccept { result: TAsyncResult ->
                        executeSuccessAction(result)
                    }
                    .exceptionally { ex: Throwable ->
                        processTaskException(ex)
                        null
                    }
        }

        private fun executeRetryAction() {
            try {
                if (currentRetryTimes >= maxRetryTimes) {
                    DelayedTask.startDelayedTask(Duration.ofMillis(retryInterval.toLong())) { doRetry() }
                } else {
                    doRetry()
                }
            } catch (ex: Exception) {
                logger.error("Failed to execute the retryAction, actionName:{}, contextInfo:{}", actionName, getContextInfo(contextInfoFunc), ex)
            }
        }

        private fun doRetry() {
            currentRetryTimes++
            execute()
        }

        private fun executeSuccessAction(result: TAsyncResult) {
            try {
                successAction.apply(result)
            } catch (ex: Exception) {
                logger.error("Failed to execute the successAction, actionName:{}, contextInfo:{}", actionName, getContextInfo(contextInfoFunc), ex)
            }
        }

        private fun executeFailedAction(e: Throwable, errorMessage: String?) {
            try {
                failedAction?.apply(e, errorMessage)
            } catch (ex: Exception) {
                logger.error("Failed to execute the failedAction of action:{}, contextInfo:{}", actionName, getContextInfo(contextInfoFunc), ex)
            }
        }

        private fun getContextInfo(func: Func<String?>): String? {
            return try {
                func.apply()
            } catch (ex: Exception) {
                logger.error("Failed to execute the getContextInfoFunc.", ex)
                ""
            }
        }

        private fun processTaskException(exception: Throwable) {
            if (exception is IORuntimeException) {
                logger.error("Async task '{}' has io exception, contextInfo:{}, current retryTimes:{}, try to run the async task again.", actionName, getContextInfo(contextInfoFunc), currentRetryTimes, exception)
                executeRetryAction()
            } else if (exception is CompletionException && exception.cause is IORuntimeException) {
                logger.error("Async task '{}' has io exception, contextInfo:{}, current retryTimes:{}, try to run the async task again.", actionName, getContextInfo(contextInfoFunc), currentRetryTimes, exception)
                executeRetryAction()
            } else {
                logger.error("Task '{}' has unknown exception, contextInfo:{}, current retryTimes:{}", actionName, getContextInfo(contextInfoFunc), currentRetryTimes, exception)
                if (retryWhenFailed) {
                    executeRetryAction()
                } else {
                    executeFailedAction(exception, exception.message)
                }
            }
        }

        init {
            this.successAction = successAction
            this.contextInfoFunc = contextInfoFunc
            this.failedAction = failedAction
            this.currentRetryTimes = retryTimes
            this.retryWhenFailed = retryWhenFailed
            this.maxRetryTimes = maxRetryTimes
            this.retryInterval = retryInterval
            this.asyncAction = asyncAction
        }
    }
}
package org.enodeframework.common.io;

import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.function.Action;
import org.enodeframework.common.function.Action1;
import org.enodeframework.common.function.Action2;
import org.enodeframework.common.function.DelayedTask;
import org.enodeframework.common.function.Func;
import org.enodeframework.common.utilities.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * @author anruence@gmail.com
 */
public class IOHelper {
    private static final Logger logger = LoggerFactory.getLogger(IOHelper.class);

    public static <TAsyncResult> void tryAsyncActionRecursively(
            String asyncActionName,
            Func<CompletableFuture<TAsyncResult>> asyncAction,
            Action1<TAsyncResult> successAction,
            Func<String> getContextInfoFunc,
            Action2<Throwable, String> failedAction,
            int retryTimes,
            boolean retryWhenFailed) {
        tryAsyncActionRecursively(asyncActionName, asyncAction, successAction, getContextInfoFunc, failedAction, retryTimes, retryWhenFailed, 3, 1000);
    }

    public static <TAsyncResult> void tryAsyncActionRecursively(
            String asyncActionName,
            Func<CompletableFuture<TAsyncResult>> asyncAction,
            Action1<TAsyncResult> successAction,
            Func<String> getContextInfoFunc,
            Action2<Throwable, String> failedAction,
            int retryTimes,
            boolean retryWhenFailed,
            int maxRetryTimes,
            int retryInterval) {
        AsyncTaskExecutionContext<TAsyncResult> asyncTaskExecutionContext = new AsyncTaskExecutionContext<>(asyncActionName, asyncAction, successAction, getContextInfoFunc, failedAction, retryTimes, retryWhenFailed, maxRetryTimes, retryInterval);
        asyncTaskExecutionContext.execute();
    }

    public static <TAsyncResult> void tryAsyncActionRecursivelyWithoutResult(
            String asyncActionName,
            Func<CompletableFuture<TAsyncResult>> asyncAction,
            Action1<TAsyncResult> successAction,
            Func<String> getContextInfoFunc,
            Action2<Throwable, String> failedAction,
            int retryTimes
    ) {
        AsyncTaskExecutionContext<TAsyncResult> asyncTaskExecutionContext = new AsyncTaskExecutionContext<>(asyncActionName, asyncAction, successAction, getContextInfoFunc, failedAction, retryTimes, false, 3, 1000);
        asyncTaskExecutionContext.execute();
    }


    public static <TAsyncResult> void tryAsyncActionRecursivelyWithoutResult(
            String asyncActionName,
            Func<CompletableFuture<TAsyncResult>> asyncAction,
            Action1<TAsyncResult> successAction,
            Func<String> getContextInfoFunc,
            Action2<Throwable, String> failedAction,
            int retryTimes,
            boolean retryWhenFailed) {
        AsyncTaskExecutionContext<TAsyncResult> asyncTaskExecutionContext = new AsyncTaskExecutionContext<>(asyncActionName, asyncAction, successAction, getContextInfoFunc, failedAction, retryTimes, retryWhenFailed, 3, 1000);
        asyncTaskExecutionContext.execute();
    }


    public static void tryIOAction(Action action, String actionName) {
        Ensure.notNull(action, "action");
        Ensure.notNull(actionName, "actionName");
        try {
            action.apply();
        } catch (Exception ex) {
            throw new IORuntimeException(String.format("%s failed.", actionName), ex);
        }
    }

    public static <T> T tryIOFunc(Func<T> func, String funcName) {
        Ensure.notNull(func, "func");
        Ensure.notNull(funcName, "funcName");
        try {
            return func.apply();
        } catch (Exception ex) {
            throw new IORuntimeException(String.format("%s failed.", funcName), ex);
        }
    }

    public static <T> CompletableFuture<T> tryIOFuncAsync(Func<CompletableFuture<T>> func, String funcName) {
        Ensure.notNull(func, "func");
        Ensure.notNull(funcName, "funcName");
        try {
            return func.apply();
        } catch (Exception ex) {
            throw new IORuntimeException(String.format("%s failed.", funcName), ex);
        }
    }

    static class AsyncTaskExecutionContext<TAsyncResult> extends AbstractTaskExecutionContext {
        private Func<CompletableFuture<TAsyncResult>> asyncAction;

        AsyncTaskExecutionContext(
                String actionName, Func<CompletableFuture<TAsyncResult>> asyncAction,
                Action1<TAsyncResult> successAction, Func<String> contextInfoFunc, Action2<Throwable, String> failedAction,
                int retryTimes, boolean retryWhenFailed, int maxRetryTimes, int retryInterval) {
            super(actionName, successAction, contextInfoFunc, failedAction, retryTimes, retryWhenFailed, maxRetryTimes, retryInterval);
            this.asyncAction = asyncAction;
        }

        @Override
        public void execute() {
            CompletableFuture<TAsyncResult> asyncResult = new CompletableFuture<>();
            try {
                asyncResult = asyncAction.apply();
            } catch (Exception ex) {
                asyncResult.completeExceptionally(ex);
            }
            taskContinueAction(asyncResult);
        }
    }


    static abstract class AbstractTaskExecutionContext<TAsyncResult> {
        private String actionName;
        private Action1<TAsyncResult> successAction;
        private Func<String> contextInfoFunc;
        private Action2<Throwable, String> failedAction;
        private int currentRetryTimes;
        private boolean retryWhenFailed;
        private int maxRetryTimes;
        private int retryInterval;

        AbstractTaskExecutionContext(String actionName, Action1<TAsyncResult> successAction, Func<String> contextInfoFunc, Action2<Throwable, String> failedAction, int retryTimes, boolean retryWhenFailed, int maxRetryTimes, int retryInterval) {
            this.actionName = actionName;
            this.successAction = successAction;
            this.contextInfoFunc = contextInfoFunc;
            this.failedAction = failedAction;
            this.currentRetryTimes = retryTimes;
            this.retryWhenFailed = retryWhenFailed;
            this.maxRetryTimes = maxRetryTimes;
            this.retryInterval = retryInterval;
        }

        public abstract void execute();

        public void taskContinueAction(CompletableFuture<TAsyncResult> asyncResult) {
            if (asyncResult.isCancelled()) {
                asyncResult.exceptionally(ex -> {
                    logger.error("Task '{}' was cancelled, contextInfo:{}, current retryTimes: {}.",
                            actionName,
                            getContextInfo(contextInfoFunc),
                            currentRetryTimes, ex);
                    executeFailedAction(ex, String.format("Task '%s' was cancelled.", actionName));
                    return null;
                });
                return;
            }
            asyncResult.thenAccept(result -> {
                executeSuccessAction(result);
            }).exceptionally(ex -> {
                processTaskException(ex);
                return null;
            });
        }

        private void executeRetryAction() {
            try {
                if (currentRetryTimes >= maxRetryTimes) {
                    DelayedTask.startDelayedTask(Duration.ofMillis(retryInterval), this::doRetry);
                } else {
                    doRetry();
                }
            } catch (Exception ex) {
                logger.error("Failed to execute the retryAction, actionName:{}, contextInfo:{}", actionName, getContextInfo(contextInfoFunc), ex);
            }
        }

        private void doRetry() {
            currentRetryTimes++;
            execute();
        }

        private void executeSuccessAction(TAsyncResult result) {
            if (successAction != null) {
                try {
                    successAction.apply(result);
                } catch (Exception ex) {
                    logger.error("Failed to execute the successAction, actionName:{}, contextInfo:{}", actionName, getContextInfo(contextInfoFunc), ex);
                }
            }
        }

        private void executeFailedAction(Throwable e, String errorMessage) {
            try {
                if (failedAction != null) {
                    failedAction.apply(e, errorMessage);
                }
            } catch (Exception ex) {
                logger.error("Failed to execute the failedAction of action:{}, contextInfo:{}", actionName, getContextInfo(contextInfoFunc), ex);
            }
        }

        private String getContextInfo(Func<String> func) {
            try {
                return func.apply();
            } catch (Exception ex) {
                logger.error("Failed to execute the getContextInfoFunc.", ex);
                return "";
            }
        }

        private void processTaskException(Throwable exception) {
            if (exception instanceof IORuntimeException) {
                logger.error("Async task '{}' has io exception, contextInfo:{}, current retryTimes:{}, try to run the async task again.", actionName, getContextInfo(contextInfoFunc), currentRetryTimes, exception);
                executeRetryAction();
            } else if (exception instanceof CompletionException && exception.getCause() instanceof IORuntimeException) {
                logger.error("Async task '{}' has io exception, contextInfo:{}, current retryTimes:{}, try to run the async task again.", actionName, getContextInfo(contextInfoFunc), currentRetryTimes, exception);
                executeRetryAction();
            } else {
                logger.error("Task '{}' has unknown exception, contextInfo:{}, current retryTimes:{}", actionName, getContextInfo(contextInfoFunc), currentRetryTimes, exception);
                if (retryWhenFailed) {
                    executeRetryAction();
                } else {
                    executeFailedAction(exception, exception.getMessage());
                }
            }
        }
    }
}

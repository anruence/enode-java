package com.enodeframework.common.io;

import com.enodeframework.common.exception.IORuntimeException;
import com.enodeframework.common.function.Action;
import com.enodeframework.common.function.Action1;
import com.enodeframework.common.function.DelayedTask;
import com.enodeframework.common.function.Func;
import com.enodeframework.common.utilities.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class IOHelper {
    private static final Logger logger = LoggerFactory.getLogger(IOHelper.class);

    public static <TAsyncResult extends AsyncTaskResult> void tryAsyncActionRecursively(
            String asyncActionName,
            Func<CompletableFuture<TAsyncResult>> asyncAction,
            Action1<Integer> mainAction,
            Action1<TAsyncResult> successAction,
            Func<String> getContextInfoFunc,
            Action1<String> failedAction,
            int retryTimes,
            boolean retryWhenFailed) {
        tryAsyncActionRecursively(asyncActionName, asyncAction, mainAction, successAction, getContextInfoFunc, failedAction, retryTimes, retryWhenFailed, 3, 1000);
    }

    public static <TAsyncResult extends AsyncTaskResult> void tryAsyncActionRecursively(
            String asyncActionName,
            Func<CompletableFuture<TAsyncResult>> asyncAction,
            Action1<Integer> mainAction,
            Action1<TAsyncResult> successAction,
            Func<String> getContextInfoFunc,
            Action1<String> failedAction,
            int retryTimes,
            boolean retryWhenFailed,
            int maxRetryTimes,
            int retryInterval) {
        AsyncTaskExecutionContext<TAsyncResult> asyncTaskExecutionContext = new AsyncTaskExecutionContext<>(asyncActionName, asyncAction, mainAction,
                successAction, getContextInfoFunc, failedAction, retryTimes, retryWhenFailed, maxRetryTimes, retryInterval);
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

    static class SyncTaskExecutionContext<TAsyncResult extends AsyncTaskResult> extends AbstractTaskExecutionContext<TAsyncResult> {
        private Func<TAsyncResult> action;

        SyncTaskExecutionContext(String actionName, Func<TAsyncResult> action, Action1<Integer> mainAction, Action1<TAsyncResult> successAction,
                                 Func<String> contextInfoFunc, Action1<String> failedAction, int retrtTimes,
                                 boolean retryWhenFailed, int maxRetryTimes, int retryInterval) {
            super(actionName, mainAction, successAction, contextInfoFunc, failedAction, retrtTimes, retryWhenFailed, maxRetryTimes, retryInterval);
            this.action = action;
        }

        @Override
        public void execute() {
            TAsyncResult result = null;
            Exception ex = null;
            try {
                result = action.apply();
            } catch (Exception e) {
                ex = e;
            }
            taskContinueAction(result, ex);
        }
    }

    static class AsyncTaskExecutionContext<TAsyncResult extends AsyncTaskResult> extends AbstractTaskExecutionContext<TAsyncResult> {
        private Func<CompletableFuture<TAsyncResult>> asyncAction;

        AsyncTaskExecutionContext(
                String actionName, Func<CompletableFuture<TAsyncResult>> asyncAction, Action1<Integer> mainAction,
                Action1<TAsyncResult> successAction, Func<String> contextInfoFunc, Action1<String> failedAction,
                int retryTimes, boolean retryWhenFailed, int maxRetryTimes, int retryInterval) {
            super(actionName, mainAction, successAction, contextInfoFunc, failedAction, retryTimes, retryWhenFailed, maxRetryTimes, retryInterval);
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
            asyncResult.thenAccept(result -> {
                taskContinueAction(result, null);
            }).exceptionally(ex -> {
                taskContinueAction(null, ex.getCause());
                return null;
            });
        }
    }

    static abstract class AbstractTaskExecutionContext<TAsyncResult extends AsyncTaskResult> {
        private String actionName;
        private Action1<Integer> mainAction;
        private Action1<TAsyncResult> successAction;
        private Func<String> contextInfoFunc;
        private Action1<String> failedAction;
        private int currentRetryTimes;
        private boolean retryWhenFailed;
        private int maxRetryTimes;
        private int retryInterval;

        AbstractTaskExecutionContext(String actionName, Action1<Integer> mainAction, Action1<TAsyncResult> successAction, Func<String> contextInfoFunc,
                                     Action1<String> failedAction, int retryTimes, boolean retryWhenFailed, int maxRetryTimes, int retryInterval) {
            this.actionName = actionName;
            this.mainAction = mainAction;
            this.successAction = successAction;
            this.contextInfoFunc = contextInfoFunc;
            this.failedAction = failedAction;
            this.currentRetryTimes = retryTimes;
            this.retryWhenFailed = retryWhenFailed;
            this.maxRetryTimes = maxRetryTimes;
            this.retryInterval = retryInterval;
        }

        public abstract void execute();

        void taskContinueAction(TAsyncResult result, Throwable ex) {
            if (ex != null) {
                if (ex instanceof CancellationException) {
                    logger.error("Task '{}' was cancelled, contextInfo:{}, current retryTimes:{}.",
                            actionName,
                            getContextInfo(contextInfoFunc),
                            currentRetryTimes);
                    executeFailedAction(String.format("Task '%s' was cancelled.", actionName));
                    return;
                }
                processTaskException(ex);
                return;
            }
            if (result == null) {
                logger.error("Task '{}' result is null, contextInfo:{}, current retryTimes:{}",
                        actionName,
                        getContextInfo(contextInfoFunc),
                        currentRetryTimes);
                if (retryWhenFailed) {
                    executeRetryAction();
                } else {
                    executeFailedAction(String.format("Async task '%s' result is null.", actionName));
                }
                return;
            }
            if (result.getStatus().equals(AsyncTaskStatus.Success)) {
                executeSuccessAction(result);
            } else if (result.getStatus().equals(AsyncTaskStatus.IOException)) {
                logger.error("Task '{}' result status is io exception, contextInfo:{}, current retryTimes:{}, errorMsg:{}, try to run the async task again.",
                        actionName,
                        getContextInfo(contextInfoFunc),
                        currentRetryTimes,
                        result.getErrorMessage());
                executeRetryAction();
            } else if (result.getStatus().equals(AsyncTaskStatus.Failed)) {
                logger.error("Task '{}' failed, contextInfo:{}, current retryTimes:{}, errorMsg:{}",
                        actionName,
                        getContextInfo(contextInfoFunc),
                        currentRetryTimes,
                        result.getErrorMessage());
                if (retryWhenFailed) {
                    executeRetryAction();
                } else {
                    executeFailedAction(result.getErrorMessage());
                }
            }
        }

        private void executeRetryAction() {
            try {
                if (currentRetryTimes >= maxRetryTimes) {
                    DelayedTask.startDelayedTask(Duration.ofMillis(retryInterval), this::doRetry);
                } else {
                    doRetry();
                }
            } catch (Exception ex) {
                logger.error("Failed to execute the retryAction, actionName:{}, contextInfo:{}", actionName, getContextInfo(contextInfoFunc), ex)
                ;
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

        private void executeFailedAction(String errorMessage) {
            try {
                if (failedAction != null) {
                    failedAction.apply(errorMessage);
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
                return null;
            }
        }

        private void processTaskException(Throwable exception) {
            if (exception instanceof IORuntimeException) {
                logger.error("Task '{}' has io exception, contextInfo:{}, current retryTimes:{}, try to run the async task again.", actionName, getContextInfo(contextInfoFunc), currentRetryTimes, exception);
                executeRetryAction();
            } else {
                logger.error("Task '{}' has unknown exception, contextInfo:{}, current retryTimes:{}", actionName, getContextInfo(contextInfoFunc), currentRetryTimes, exception);
                if (retryWhenFailed) {
                    executeRetryAction();
                } else {
                    executeFailedAction(exception.getMessage());
                }
            }
        }
    }
}

package org.enodeframework.commanding;

import org.enodeframework.applicationmessage.IApplicationMessage;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface ICommandAsyncHandlerProxy extends IObjectProxy, MethodInvocation {
    /**
     * Handle the given application command async.
     *
     * @param command
     * @return
     */
    CompletableFuture<AsyncTaskResult<IApplicationMessage>> handleAsync(ICommand command);
}

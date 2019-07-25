package com.enodeframework.commanding;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.infrastructure.IObjectProxy;
import com.enodeframework.infrastructure.MethodInvocation;

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

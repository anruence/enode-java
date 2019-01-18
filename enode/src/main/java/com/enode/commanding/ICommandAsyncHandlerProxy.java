package com.enode.commanding;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IObjectProxy;
import com.enode.infrastructure.MethodInvocation;

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

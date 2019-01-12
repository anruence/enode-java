package com.qianzhui.enode.commanding;

import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.infrastructure.IApplicationMessage;
import com.qianzhui.enode.infrastructure.IObjectProxy;
import com.qianzhui.enode.infrastructure.MethodInvocation;

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

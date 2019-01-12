package com.qianzhui.enode.commanding;

import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.infrastructure.IApplicationMessage;

import java.util.concurrent.CompletableFuture;

public interface ICommandAsyncHandler {
    /**
     * Handle the given command async.
     *
     * @param command
     * @return
     */
    CompletableFuture<AsyncTaskResult<IApplicationMessage>> handleAsync(ICommand command);
}

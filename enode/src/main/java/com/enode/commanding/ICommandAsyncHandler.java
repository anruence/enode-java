package com.enode.commanding;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IApplicationMessage;

import java.util.concurrent.CompletableFuture;

public interface ICommandAsyncHandler<T extends ICommand> {
    /**
     * Handle the given command async.
     *
     * @param command
     * @return
     */
    CompletableFuture<AsyncTaskResult<IApplicationMessage>> handleAsync(T command);
}

package com.qianzhui.enode.commanding;

import com.qianzhui.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface ICommandService {

    CompletableFuture<AsyncTaskResult> sendAsync(ICommand command);

    CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command);

    CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command, CommandReturnType commandReturnType);
}

package com.enodeframework.commanding;

import java.util.concurrent.CompletableFuture;

public interface IProcessingCommandHandler {
    CompletableFuture<Void> handleAsync(ProcessingCommand processingCommand);
}

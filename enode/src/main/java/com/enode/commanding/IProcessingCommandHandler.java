package com.enode.commanding;

import java.util.concurrent.CompletableFuture;

public interface IProcessingCommandHandler {
    CompletableFuture handle(ProcessingCommand processingCommand);
}

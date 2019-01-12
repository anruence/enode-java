package com.qianzhui.enode.commanding;

import java.util.concurrent.CompletableFuture;

public interface IProcessingCommandHandler {
    CompletableFuture handle(ProcessingCommand processingCommand);
}

package com.enodeframework.commanding;

import com.enodeframework.infrastructure.DefaultMailBox;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */

public class ProcessingCommandMailbox extends DefaultMailBox<ProcessingCommand, CommandResult> {

    private static int commandMailBoxPersistenceMaxBatchSize = 1000;

    public ProcessingCommandMailbox(String aggregateRootId, IProcessingCommandHandler messageHandler) {
        super(aggregateRootId, commandMailBoxPersistenceMaxBatchSize, false, (messageHandler::handle), null);
    }

    @Override
    protected CompletableFuture<Void> completeMessageWithResult(ProcessingCommand processingCommand, CommandResult commandResult) {
        return processingCommand.completeAsync(commandResult).exceptionally(ex -> {
            logger.error("Failed to complete command, commandId: {}, aggregateRootId: {}", processingCommand.getMessage().getId(), processingCommand.getMessage().getAggregateRootId(), ex);
            return null;
        });
    }

}

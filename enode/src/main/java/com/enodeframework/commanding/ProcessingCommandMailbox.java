package com.enodeframework.commanding;

import com.enodeframework.infrastructure.DefaultMailBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */

public class ProcessingCommandMailbox extends DefaultMailBox<ProcessingCommand, CommandResult> {
    private static final Logger logger = LoggerFactory.getLogger(ProcessingCommandMailbox.class);

    private static int commandMailBoxPersistenceMaxBatchSize;

    public ProcessingCommandMailbox(String aggregateRootId, IProcessingCommandHandler messageHandler) {
        super(aggregateRootId, commandMailBoxPersistenceMaxBatchSize, false, (x -> messageHandler.handle(x)), null);

    }

    protected CompletableFuture<Void> completeMessageWithResult(ProcessingCommand processingCommand, CommandResult commandResult) {
        return processingCommand.completeAsync(commandResult).exceptionally(ex -> {
            _logger.error("Failed to complete command, commandId: {}, aggregateRootId: {}", processingCommand.getMessage().id(), processingCommand.getMessage().getAggregateRootId(), ex);
            return null;
        });
    }

}

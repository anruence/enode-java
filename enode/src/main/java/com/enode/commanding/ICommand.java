package com.enode.commanding;

import com.enode.infrastructure.IMessage;

/**
 * Represents a command.
 */
public interface ICommand extends IMessage {
    /**
     * Represents the associated aggregate root string id.
     */
    String getAggregateRootId();
}

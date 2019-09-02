package com.enodeframework.commanding;

import com.enodeframework.messaging.IMessage;

/**
 * Represents a command.
 */
public interface ICommand extends IMessage {
    /**
     * Represents the associated aggregate root string id.
     */
    String getAggregateRootId();
}

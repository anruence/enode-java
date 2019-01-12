package com.qianzhui.enode.commanding;

import com.qianzhui.enode.infrastructure.IMessage;

/**
 *
 * Represents a command.
 */
public interface ICommand extends IMessage {
    /**
     * Represents the associated aggregate root string id.
     */
    String getAggregateRootId();
}

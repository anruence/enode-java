package com.enode.commanding;

public interface ICommandRoutingKeyProvider {
    /**
     * Returns a routing key for the given command.
     */
    String getRoutingKey(ICommand command);
}

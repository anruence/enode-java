package com.enode.commanding.impl;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandRoutingKeyProvider;

public class DefaultCommandRoutingKeyProvider implements ICommandRoutingKeyProvider {
    @Override
    public String getRoutingKey(ICommand command) {
        if (!(command.getAggregateRootId() == null || "".equals(command.getAggregateRootId().trim()))) {
            return command.getAggregateRootId();
        }

        return command.id();
    }
}

package com.qianzhui.enode.commanding.impl;

import com.qianzhui.enode.commanding.ICommand;
import com.qianzhui.enode.commanding.ICommandRoutingKeyProvider;

public class DefaultCommandRoutingKeyProvider implements ICommandRoutingKeyProvider {
    @Override
    public String getRoutingKey(ICommand command) {
        if (!(command.getAggregateRootId() == null || "".equals(command.getAggregateRootId().trim()))) {
            return command.getAggregateRootId();
        }

        return command.id();
    }
}

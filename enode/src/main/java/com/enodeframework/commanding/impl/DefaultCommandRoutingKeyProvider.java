package com.enodeframework.commanding.impl;

import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandRoutingKeyProvider;

/**
 * @author anruence@gmail.com
 */
public class DefaultCommandRoutingKeyProvider implements ICommandRoutingKeyProvider {
    @Override
    public String getRoutingKey(ICommand command) {
        if (!(command.getAggregateRootId() == null || "".equals(command.getAggregateRootId().trim()))) {
            return command.getAggregateRootId();
        }
        return command.getId();
    }
}

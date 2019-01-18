package com.enode.message;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandKeyProvider;

public class CommandKeyProvider implements ICommandKeyProvider {
    @Override
    public String getKey(ICommand command) {
        return command.getAggregateRootId() == null ? command.id() : command.getAggregateRootId();
    }
}

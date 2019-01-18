package com.qianzhui.enode.message;

import com.qianzhui.enode.commanding.ICommand;
import com.qianzhui.enode.commanding.ICommandKeyProvider;

public class CommandKeyProvider implements ICommandKeyProvider {
    @Override
    public String getKey(ICommand command) {
        return command.getAggregateRootId() == null ? command.id() : command.getAggregateRootId();
    }
}

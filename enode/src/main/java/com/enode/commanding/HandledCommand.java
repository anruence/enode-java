package com.enode.commanding;

import com.enode.infrastructure.IApplicationMessage;

public class HandledCommand {
    private String commandId;
    private String aggregateRootId;
    private IApplicationMessage message;

    public HandledCommand() {

    }

    public HandledCommand(String commandId, String aggregateRootId, IApplicationMessage message) {
        this.commandId = commandId;
        this.aggregateRootId = aggregateRootId;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("[CommandId=%s,AggregateRootId=%s,Message=%s]",
                commandId,
                aggregateRootId,
                message == null ? null : String.format("[id:%s,type:%s]", message.id(), message.getClass().getName()));
    }

    public String getCommandId() {
        return commandId;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public IApplicationMessage getMessage() {
        return message;
    }
}

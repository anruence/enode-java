package com.enodeframework.common.utilities;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.queue.domainevent.DomainEventHandledMessage;

/**
 * remote relay used by vert.x
 */

/**
 * @author anruence@gmail.com
 */
public class RemoteReply {

    private int code;

    private CommandResult commandResult;

    private DomainEventHandledMessage eventHandledMessage;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public CommandResult getCommandResult() {
        return commandResult;
    }

    public void setCommandResult(CommandResult commandResult) {
        this.commandResult = commandResult;
    }

    public DomainEventHandledMessage getEventHandledMessage() {
        return eventHandledMessage;
    }

    public void setEventHandledMessage(DomainEventHandledMessage eventHandledMessage) {
        this.eventHandledMessage = eventHandledMessage;
    }

    @Override
    public String toString() {
        return "RemoteReply{" +
                "code=" + code +
                ", commandResult=" + commandResult +
                ", eventHandledMessage=" + eventHandledMessage +
                '}';
    }
}

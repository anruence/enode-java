package org.enodeframework.queue.command;

import org.enodeframework.common.io.ReplySocketAddress;

import java.io.Serializable;

/**
 * @author anruence@gmail.com
 */
public class CommandMessage implements Serializable {
    private String commandType;
    private String commandData;
    private ReplySocketAddress replyAddress;

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandData() {
        return commandData;
    }

    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }

    public ReplySocketAddress getReplyAddress() {
        return replyAddress;
    }

    public void setReplyAddress(ReplySocketAddress replyAddress) {
        this.replyAddress = replyAddress;
    }

}

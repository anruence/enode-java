package org.enodeframework.queue.command;

import java.net.InetSocketAddress;

/**
 * @author anruence@gmail.com
 */
public class CommandMessage {
    private String commandData;

    public InetSocketAddress getReplyAddress() {
        return replyAddress;
    }

    public void setReplyAddress(InetSocketAddress replyAddress) {
        this.replyAddress = replyAddress;
    }

    private InetSocketAddress replyAddress;
    private String commandType;

    public String getCommandData() {
        return commandData;
    }

    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }


    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}

package com.enode.queue.command;

public class CommandMessage {
    private String commandData;
    private String replyAddress;
    private String commandType;

    public CommandMessage(String commandData, String replyAddress, String commandType) {
        this.commandData = commandData;
        this.replyAddress = replyAddress;
        this.commandType = commandType;
    }

    public String getCommandData() {
        return commandData;
    }

    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }

    public String getReplyAddress() {
        return replyAddress;
    }

    public void setReplyAddress(String replyAddress) {
        this.replyAddress = replyAddress;
    }

    public String getCommandType() {
        return commandType;
    }
}

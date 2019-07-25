package com.enodeframework.commanding;

import com.enodeframework.infrastructure.IMailBox;
import com.enodeframework.infrastructure.IMailBoxMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class ProcessingCommand implements IMailBoxMessage<ProcessingCommand, CommandResult> {
    private final ICommand message;
    private final ICommandExecuteContext commandExecuteContext;
    private final Map<String, String> items;
    private IMailBox<ProcessingCommand, CommandResult> mailBox;
    private long sequence;

    public ProcessingCommand(ICommand command, ICommandExecuteContext commandExecuteContext, Map<String, String> items) {
        this.message = command;
        this.commandExecuteContext = commandExecuteContext;
        this.items = items == null ? new HashMap<>() : items;
    }

    public CompletableFuture<Void> completeAsync(CommandResult commandResult) {
        return commandExecuteContext.onCommandExecutedAsync(commandResult);
    }

    @Override
    public IMailBox<ProcessingCommand, CommandResult> getMailBox() {
        return mailBox;
    }

    @Override
    public void setMailBox(IMailBox mailBox) {
        this.mailBox = mailBox;
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public ICommand getMessage() {
        return message;
    }

    public ICommandExecuteContext getCommandExecuteContext() {
        return commandExecuteContext;
    }

    public Map<String, String> getItems() {
        return items;
    }
}

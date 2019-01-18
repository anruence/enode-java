package com.enode.commanding;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProcessingCommand {
    private final ICommand message;
    private final ICommandExecuteContext commandExecuteContext;
    private final Map<String, String> items;
    private ProcessingCommandMailbox mailbox;
    private long sequence;

    public ProcessingCommand(ICommand command, ICommandExecuteContext commandExecuteContext, Map<String, String> items) {
        this.message = command;
        this.commandExecuteContext = commandExecuteContext;
        this.items = items == null ? new HashMap<>() : items;
    }

    public CompletableFuture completeAsync(CommandResult commandResult) {
        return commandExecuteContext.onCommandExecutedAsync(commandResult);
    }

    public ProcessingCommandMailbox getMailbox() {
        return mailbox;
    }

    public void setMailbox(ProcessingCommandMailbox mailbox) {
        this.mailbox = mailbox;
    }

    public long getSequence() {
        return sequence;
    }

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

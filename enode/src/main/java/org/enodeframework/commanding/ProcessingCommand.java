package org.enodeframework.commanding;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class ProcessingCommand {
    private final ICommand message;
    private final ICommandExecuteContext commandExecuteContext;
    private final Map<String, String> items;
    private ProcessingCommandMailbox mailBox;
    private long sequence;

    private boolean duplicated;

    public ProcessingCommand(ICommand command, ICommandExecuteContext commandExecuteContext, Map<String, String> items) {
        this.message = command;
        this.commandExecuteContext = commandExecuteContext;
        this.items = items == null ? new HashMap<>() : items;
    }

    public CompletableFuture<Void> completeAsync(CommandResult commandResult) {
        return commandExecuteContext.onCommandExecutedAsync(commandResult);
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

    public ProcessingCommandMailbox getMailBox() {
        return mailBox;
    }

    public void setMailBox(ProcessingCommandMailbox mailBox) {
        this.mailBox = mailBox;
    }

    public boolean isDuplicated() {
        return duplicated;
    }

    public void setDuplicated(boolean duplicated) {
        this.duplicated = duplicated;
    }
}

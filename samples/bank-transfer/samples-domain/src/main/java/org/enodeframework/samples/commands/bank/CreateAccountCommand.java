package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

public class CreateAccountCommand extends Command<String> {
    public String owner;

    public CreateAccountCommand() {
    }

    public CreateAccountCommand(String accountId, String owner) {
        super(accountId);
        this.owner = owner;
    }
}

package com.enodeframework.samples.commands.bank;

import com.enodeframework.commanding.Command;

public class CreateAccountCommand extends Command {
    public String Owner;

    public CreateAccountCommand() {
    }

    public CreateAccountCommand(String accountId, String owner) {

        super(accountId);
        Owner = owner;
    }
}

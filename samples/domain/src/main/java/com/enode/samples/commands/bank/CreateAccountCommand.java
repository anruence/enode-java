package com.enode.samples.commands.bank;

import com.enode.commanding.Command;

public class CreateAccountCommand extends Command {
    public String Owner;

    public CreateAccountCommand() {
    }

    public CreateAccountCommand(String accountId, String owner) {

        super(accountId);
        Owner = owner;
    }
}

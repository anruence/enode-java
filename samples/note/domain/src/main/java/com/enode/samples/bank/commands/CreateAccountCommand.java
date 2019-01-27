package com.enode.samples.bank.commands;

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

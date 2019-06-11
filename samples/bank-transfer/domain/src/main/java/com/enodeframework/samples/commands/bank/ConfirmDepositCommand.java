package com.enodeframework.samples.commands.bank;

import com.enodeframework.commanding.Command;

/// <summary>确认存款
/// </summary>
public class ConfirmDepositCommand extends Command {
    public ConfirmDepositCommand() {
    }

    public ConfirmDepositCommand(String transactionId) {
        super(transactionId);
    }
}

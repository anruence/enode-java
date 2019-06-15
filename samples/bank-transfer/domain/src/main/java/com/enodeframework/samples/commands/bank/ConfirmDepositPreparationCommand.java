package com.enodeframework.samples.commands.bank;

import com.enodeframework.commanding.Command;

/// <summary>确认预存款
/// </summary>
public class ConfirmDepositPreparationCommand extends Command {
    public ConfirmDepositPreparationCommand() {
    }

    public ConfirmDepositPreparationCommand(String transactionId) {
        super(transactionId);
    }
}

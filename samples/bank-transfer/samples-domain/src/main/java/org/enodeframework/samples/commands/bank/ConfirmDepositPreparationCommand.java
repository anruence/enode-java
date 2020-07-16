package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 确认预存款
 */
public class ConfirmDepositPreparationCommand extends Command {
    public ConfirmDepositPreparationCommand() {
    }

    public ConfirmDepositPreparationCommand(String transactionId) {
        super(transactionId);
    }
}

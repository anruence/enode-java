package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 确认存款
 */
public class ConfirmDepositCommand extends Command {
    public ConfirmDepositCommand() {
    }

    public ConfirmDepositCommand(String transactionId) {
        super(transactionId);
    }
}

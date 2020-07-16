package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 确认预转出
 */
public class ConfirmTransferOutPreparationCommand extends Command {
    public ConfirmTransferOutPreparationCommand() {
    }

    public ConfirmTransferOutPreparationCommand(String transactionId) {
        super(transactionId);
    }
}

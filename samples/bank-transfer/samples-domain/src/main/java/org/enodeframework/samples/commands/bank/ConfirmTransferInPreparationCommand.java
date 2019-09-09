package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/// <summary>确认预转入
/// </summary>
public class ConfirmTransferInPreparationCommand extends Command {
    public ConfirmTransferInPreparationCommand() {
    }

    public ConfirmTransferInPreparationCommand(String transactionId) {
        super(transactionId);
    }
}

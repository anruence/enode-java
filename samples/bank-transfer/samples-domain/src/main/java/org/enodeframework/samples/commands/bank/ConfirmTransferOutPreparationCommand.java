package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/// <summary>确认预转出
/// </summary>
public class ConfirmTransferOutPreparationCommand extends Command {
    public ConfirmTransferOutPreparationCommand() {
    }

    public ConfirmTransferOutPreparationCommand(String transactionId) {
        super(transactionId);
    }
}

package com.enodeframework.samples.commands.bank;

import com.enodeframework.commanding.Command;


/// <summary>确认预转出
/// </summary>
public class ConfirmTransferOutPreparationCommand extends Command {
    public ConfirmTransferOutPreparationCommand() {
    }

    public ConfirmTransferOutPreparationCommand(String transactionId) {
        super(transactionId);
    }
}




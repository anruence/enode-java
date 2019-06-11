package com.enodeframework.samples.commands.bank;

import com.enodeframework.commanding.Command;


/// <summary>确认转出
/// </summary>
public class ConfirmTransferOutCommand extends Command {
    public ConfirmTransferOutCommand() {
    }

    public ConfirmTransferOutCommand(String transactionId) {
        super(transactionId);
    }
}



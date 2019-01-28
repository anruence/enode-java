package com.enode.samples.commands.bank;

import com.enode.commanding.Command;


/// <summary>确认转出
/// </summary>
public class ConfirmTransferOutCommand extends Command {
    public ConfirmTransferOutCommand() {
    }

    public ConfirmTransferOutCommand(String transactionId) {
        super(transactionId);
    }
}



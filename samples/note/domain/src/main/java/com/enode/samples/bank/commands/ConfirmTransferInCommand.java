package com.enode.samples.bank.commands;

import com.enode.commanding.Command;

/// <summary>确认转入
/// </summary>
public class ConfirmTransferInCommand extends Command {
    public ConfirmTransferInCommand() {
    }

    public ConfirmTransferInCommand(String transactionId) {
        super(transactionId);
    }
}


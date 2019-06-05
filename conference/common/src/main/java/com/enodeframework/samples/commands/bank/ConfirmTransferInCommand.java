package com.enodeframework.samples.commands.bank;

import com.enodeframework.commanding.Command;

/// <summary>确认转入
/// </summary>
public class ConfirmTransferInCommand extends Command {
    public ConfirmTransferInCommand() {
    }

    public ConfirmTransferInCommand(String transactionId) {
        super(transactionId);
    }
}


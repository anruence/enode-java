package com.enode.samples.bank.commands;

import com.enode.commanding.Command;

/// <summary>取消转账交易
/// </summary>
public class CancelTransferTransactionCommand extends Command {
    public CancelTransferTransactionCommand() {
    }

    public CancelTransferTransactionCommand(String transactionId) {
        super(transactionId);
    }
}

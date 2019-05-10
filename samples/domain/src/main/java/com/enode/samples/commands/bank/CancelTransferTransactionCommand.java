package com.enode.samples.commands.bank;

import com.enode.commanding.Command;

/**
 * 取消转账交易
 */
public class CancelTransferTransactionCommand extends Command {
    public CancelTransferTransactionCommand() {
    }

    public CancelTransferTransactionCommand(String transactionId) {
        super(transactionId);
    }
}

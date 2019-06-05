package com.enodeframework.samples.commands.bank;

import com.enodeframework.commanding.Command;

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

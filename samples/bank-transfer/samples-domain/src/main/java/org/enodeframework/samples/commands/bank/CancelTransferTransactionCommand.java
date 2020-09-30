package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 取消转账交易
 */
public class CancelTransferTransactionCommand extends Command<String> {
    public CancelTransferTransactionCommand() {
    }

    public CancelTransferTransactionCommand(String transactionId) {
        super(transactionId);
    }
}

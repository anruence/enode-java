package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionInfo;

/// <summary>发起一笔转账交易
/// </summary>
public class StartTransferTransactionCommand extends Command {
    /// <summary>转账交易信息
    /// </summary>
    public TransferTransactionInfo TransactionInfo;

    public StartTransferTransactionCommand() {
    }

    public StartTransferTransactionCommand(String transactionId, TransferTransactionInfo transactionInfo) {
        super(transactionId);
        TransactionInfo = transactionInfo;
    }
}

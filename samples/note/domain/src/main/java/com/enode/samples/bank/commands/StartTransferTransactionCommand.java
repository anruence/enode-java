package com.enode.samples.bank.commands;

import com.enode.commanding.Command;
import com.enode.samples.bank.domain.transfertransaction.TransferTransactionInfo;

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





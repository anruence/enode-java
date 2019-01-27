﻿package com.enode.samples.bank.commands;

import com.enode.commanding.Command;

/// <summary>发起一笔存款交易
/// </summary>
public class StartDepositTransactionCommand extends Command {
    /// <summary>账户ID
    /// </summary>
    public String AccountId;
    /// <summary>存款金额
    /// </summary>
    public double Amount;

    public StartDepositTransactionCommand() {
    }

    public StartDepositTransactionCommand(String transactionId, String accountId, double amount) {
        super(transactionId);
        AccountId = accountId;
        Amount = amount;
    }
}

package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 发起一笔存款交易
 */
public class StartDepositTransactionCommand extends Command {
    /**
     * 账户ID
     */
    public String AccountId;
    /**
     * 存款金额
     */
    public double Amount;

    public StartDepositTransactionCommand() {
    }

    public StartDepositTransactionCommand(String transactionId, String accountId, double amount) {
        super(transactionId);
        AccountId = accountId;
        Amount = amount;
    }
}

package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 向账户添加一笔预操作
 */
public class AddTransactionPreparationCommand extends Command<String> {
    public String TransactionId;
    public int TransactionType;
    public int PreparationType;
    public double Amount;

    public AddTransactionPreparationCommand() {
    }

    public AddTransactionPreparationCommand(String accountId, String transactionId, int transactionType, int preparationType, double amount) {
        super(accountId);
        TransactionId = transactionId;
        TransactionType = transactionType;
        PreparationType = preparationType;
        Amount = amount;
    }
}

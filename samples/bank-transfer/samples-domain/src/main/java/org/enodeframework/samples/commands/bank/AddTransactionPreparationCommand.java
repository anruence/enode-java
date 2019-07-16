package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 向账户添加一笔预操作
 */
public class AddTransactionPreparationCommand extends Command<String> {
    public String transactionId;
    public int transactionType;
    public int preparationType;
    public double amount;

    public AddTransactionPreparationCommand() {
    }

    public AddTransactionPreparationCommand(String accountId, String transactionId, int transactionType, int preparationType, double amount) {
        super(accountId);
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.preparationType = preparationType;
        this.amount = amount;
    }
}

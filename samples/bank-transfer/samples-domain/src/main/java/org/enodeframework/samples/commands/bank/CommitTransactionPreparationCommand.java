package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 提交预操作
 */
public class CommitTransactionPreparationCommand extends Command<String> {
    public String transactionId;

    public CommitTransactionPreparationCommand() {
    }

    public CommitTransactionPreparationCommand(String accountId, String transactionId) {
        super(accountId);
        this.transactionId = transactionId;
    }
}

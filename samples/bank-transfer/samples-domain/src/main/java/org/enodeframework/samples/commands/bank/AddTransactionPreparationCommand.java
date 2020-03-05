package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;
import org.enodeframework.common.serializing.JacksonSerialization;
import org.enodeframework.common.serializing.JsonTool;

import java.util.Date;

/// <summary>向账户添加一笔预操作
/// </summary>
public class AddTransactionPreparationCommand extends Command {
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

    public static void main(String[] args) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand();
        command.id = "id";
        command.aggregateRootId = "aggregateRootId";
        command.TransactionId = "TransactionId";
        command.timestamp = new Date();
        System.out.println(JsonTool.serialize(command));
        System.out.println("\n");
        System.out.println(JacksonSerialization.serialize(command));
    }
}

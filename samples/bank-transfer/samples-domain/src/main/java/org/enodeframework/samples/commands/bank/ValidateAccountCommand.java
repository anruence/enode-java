package org.enodeframework.samples.commands.bank;
/// <summary>开户（创建一个账户）
/// </summary>

import org.enodeframework.commanding.Command;

/// <summary>验证账户是否合法
/// </summary>
public class ValidateAccountCommand extends Command {
    public String TransactionId;

    public ValidateAccountCommand() {
    }

    public ValidateAccountCommand(String accountId, String transactionId) {
        super(accountId);
        TransactionId = transactionId;
    }
}

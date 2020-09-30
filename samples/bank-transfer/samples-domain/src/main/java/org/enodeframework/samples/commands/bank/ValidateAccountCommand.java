package org.enodeframework.samples.commands.bank;
/**
 * 开户（创建一个账户）
 */

import org.enodeframework.commanding.Command;

/**
 * 验证账户是否合法
 */
public class ValidateAccountCommand extends Command<String> {
    public String transactionId;

    public ValidateAccountCommand() {
    }

    public ValidateAccountCommand(String accountId, String transactionId) {
        super(accountId);
        this.transactionId = transactionId;
    }
}

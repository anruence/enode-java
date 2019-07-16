package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 确认账户验证已通过
 */
public class ConfirmAccountValidatePassedCommand extends Command<String> {
    /**
     * 账户ID
     */
    public String accountId;

    public ConfirmAccountValidatePassedCommand() {
    }

    public ConfirmAccountValidatePassedCommand(String transactionId, String accountId) {
        super(transactionId);
        this.accountId = accountId;
    }
}

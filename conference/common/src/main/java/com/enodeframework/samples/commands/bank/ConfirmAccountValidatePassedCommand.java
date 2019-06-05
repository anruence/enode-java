package com.enodeframework.samples.commands.bank;

import com.enodeframework.commanding.Command;

/// <summary>确认账户验证已通过
/// </summary>
public class ConfirmAccountValidatePassedCommand extends Command {
    /// <summary>账户ID
    /// </summary>
    public String AccountId;

    public ConfirmAccountValidatePassedCommand() {
    }

    public ConfirmAccountValidatePassedCommand(String transactionId, String accountId) {
        super(transactionId);
        AccountId = accountId;
    }
}


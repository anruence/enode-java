package com.enodeframework.samples.applicationmessages;

import com.enodeframework.infrastructure.ApplicationMessage;

/// <summary>账户验证已通过
/// </summary>
public class AccountValidatePassedMessage extends ApplicationMessage {
    public String AccountId;
    public String TransactionId;

    public AccountValidatePassedMessage() {
    }

    public AccountValidatePassedMessage(String accountId, String transactionId) {
        AccountId = accountId;
        TransactionId = transactionId;
    }

    @Override
    public String getRoutingKey() {
        return AccountId;
    }
}

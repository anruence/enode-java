package com.enode.samples.applicationmessages;

import com.enode.infrastructure.ApplicationMessage;

/// <summary>账户验证未通过
/// </summary>
public class AccountValidateFailedMessage extends ApplicationMessage {
    public String AccountId;

    public String TransactionId;

    public String Reason;


    public AccountValidateFailedMessage() {
    }

    public AccountValidateFailedMessage(String accountId, String transactionId, String reason) {
        AccountId = accountId;
        TransactionId = transactionId;
        Reason = reason;
    }


    @Override
    public String getRoutingKey() {
        return AccountId;
    }
}

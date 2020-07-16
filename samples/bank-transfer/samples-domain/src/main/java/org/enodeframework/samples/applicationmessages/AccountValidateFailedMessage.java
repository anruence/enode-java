package org.enodeframework.samples.applicationmessages;

import org.enodeframework.messaging.ApplicationMessage;

/**
 * 账户验证未通过
 */
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
}

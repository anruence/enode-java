package org.enodeframework.samples.applicationmessages;

import org.enodeframework.messaging.ApplicationMessage;

/**
 * 账户验证已通过
 */
public class AccountValidatePassedMessage extends ApplicationMessage {
    public String AccountId;
    public String TransactionId;

    public AccountValidatePassedMessage() {
    }

    public AccountValidatePassedMessage(String accountId, String transactionId) {
        AccountId = accountId;
        TransactionId = transactionId;
    }
}

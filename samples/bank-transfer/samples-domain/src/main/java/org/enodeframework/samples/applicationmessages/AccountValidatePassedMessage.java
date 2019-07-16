package org.enodeframework.samples.applicationmessages;

import org.enodeframework.messaging.ApplicationMessage;

/**
 * 账户验证已通过
 */
public class AccountValidatePassedMessage extends ApplicationMessage {
    public String accountId;
    public String transactionId;

    public AccountValidatePassedMessage() {
    }

    public AccountValidatePassedMessage(String accountId, String transactionId) {
        this.accountId = accountId;
        this.transactionId = transactionId;
    }
}

package org.enodeframework.samples.domain.bank;

/**
 * 交易状态
 */
public class TransactionStatus {
    public static int STARTED = 1;
    public static int ACCOUNT_VALIDATE_COMPLETED = 2;
    public static int PREPARATION_COMPLETED = 3;
    public static int COMPLETED = 4;
    public static int CANCELED = 5;
}

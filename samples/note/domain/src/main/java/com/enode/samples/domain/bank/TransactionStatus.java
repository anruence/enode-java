package com.enode.samples.domain.bank;

/**
 * 交易状态
 */
public class TransactionStatus {
    public static int Started = 1;
    public static int AccountValidateCompleted = 2;
    public static int PreparationCompleted = 3;
    public static int Completed = 4;
    public static int Canceled = 5;
}

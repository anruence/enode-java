﻿package com.enode.samples.bank.domain.bankaccount;

/// <summary>预支出或预收入类型枚举
public class PreparationType {
    /// <summary>预支出（记入借方，When your bank debits your account, money is taken from it and paid to someone else.）
    /// </summary>
    public static int DebitPreparation = 1;
    /// <summary>预收入（记入贷方，When a sum of money is credited to an account, the bank adds that sum of money to the total in the account.）
    /// </summary>
    public static int CreditPreparation = 2;
}

package com.microsoft.conference.common.payment.commands;

import org.enodeframework.commanding.Command;

import java.math.BigDecimal;
import java.util.List;

public class CreatePayment extends Command<String> {
    public String OrderId;
    public String ConferenceId;
    public String Description;
    public BigDecimal TotalAmount;
    public List<PaymentLine> Lines;
}

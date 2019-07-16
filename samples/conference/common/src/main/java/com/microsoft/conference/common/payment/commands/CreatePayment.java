package com.microsoft.conference.common.payment.commands;

import org.enodeframework.commanding.Command;

import java.math.BigDecimal;
import java.util.List;

public class CreatePayment extends Command<String> {
    public String orderId;
    public String conferenceId;
    public String description;
    public BigDecimal totalAmount;
    public List<PaymentLine> lines;
}

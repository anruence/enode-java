package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName(value = "payment_item")
public class PaymentItemDO {
    private String id;
    private String paymentItemId;
    private String paymentId;
    private String description;
    private BigDecimal amount;

    public String getId() {
        return this.id;
    }

    public String getPaymentItemId() {
        return this.paymentItemId;
    }

    public String getPaymentId() {
        return this.paymentId;
    }

    public String getDescription() {
        return this.description;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPaymentItemId(String paymentItemId) {
        this.paymentItemId = paymentItemId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

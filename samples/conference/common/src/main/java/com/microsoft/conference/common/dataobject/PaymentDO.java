package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName(value = "payment")
public class PaymentDO {
    private String id;
    private String paymentId;
    private Integer state;
    private String orderId;
    private String description;
    private BigDecimal totalAmount;
    private Integer version;

    public String getId() {
        return this.id;
    }

    public String getPaymentId() {
        return this.paymentId;
    }

    public Integer getState() {
        return this.state;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public String getDescription() {
        return this.description;
    }

    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}

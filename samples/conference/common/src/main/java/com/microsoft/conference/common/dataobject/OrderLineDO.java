package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName(value = "order_line")
public class OrderLineDO {
    private String id;
    private String orderId;
    private String seatTypeId;
    private String seatTypeName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;

    public String getId() {
        return this.id;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public String getSeatTypeId() {
        return this.seatTypeId;
    }

    public String getSeatTypeName() {
        return this.seatTypeName;
    }

    public BigDecimal getUnitPrice() {
        return this.unitPrice;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public BigDecimal getLineTotal() {
        return this.lineTotal;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setSeatTypeId(String seatTypeId) {
        this.seatTypeId = seatTypeId;
    }

    public void setSeatTypeName(String seatTypeName) {
        this.seatTypeName = seatTypeName;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}

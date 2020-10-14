package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;

@TableName(value = "order")
public class OrderDO {
    private String id;
    private String orderId;
    private String conferenceId;
    private Integer status;
    private String accessCode;
    private String registrantFirstName;
    private String registrantLastName;
    private String registrantEmail;
    private BigDecimal totalAmount;
    private Date reservationExpirationDate;
    private Long version;

    public String getId() {
        return this.id;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public String getConferenceId() {
        return this.conferenceId;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getAccessCode() {
        return this.accessCode;
    }

    public String getRegistrantFirstName() {
        return this.registrantFirstName;
    }

    public String getRegistrantLastName() {
        return this.registrantLastName;
    }

    public String getRegistrantEmail() {
        return this.registrantEmail;
    }

    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    public Date getReservationExpirationDate() {
        return this.reservationExpirationDate;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setConferenceId(String conferenceId) {
        this.conferenceId = conferenceId;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public void setRegistrantFirstName(String registrantFirstName) {
        this.registrantFirstName = registrantFirstName;
    }

    public void setRegistrantLastName(String registrantLastName) {
        this.registrantLastName = registrantLastName;
    }

    public void setRegistrantEmail(String registrantEmail) {
        this.registrantEmail = registrantEmail;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setReservationExpirationDate(Date reservationExpirationDate) {
        this.reservationExpirationDate = reservationExpirationDate;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

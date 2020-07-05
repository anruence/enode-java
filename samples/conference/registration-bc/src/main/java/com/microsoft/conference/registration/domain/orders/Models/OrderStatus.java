package com.microsoft.conference.registration.domain.orders.Models;

public enum OrderStatus {
    Placed(1),                //订单已生成
    ReservationSuccess(2),    //位置预定已成功（下单已成功）
    ReservationFailed(3),     //位置预定已失败（下单失败）
    PaymentSuccess(4),        //付款已成功
    PaymentRejected(5),       //付款已拒绝
    Expired(6),               //订单已过期
    Success(7),               //交易已成功
    Closed(8);              //订单已关闭
    private int status;

    OrderStatus(int status) {
        this.status = (status);
    }
}

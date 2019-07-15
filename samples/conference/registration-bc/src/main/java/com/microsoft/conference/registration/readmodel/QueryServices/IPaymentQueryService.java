package com.microsoft.conference.registration.readmodel.QueryServices;

public interface IPaymentQueryService {
    Payment FindPayment(String paymentId);
}

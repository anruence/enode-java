package com.microsoft.conference.registration.readmodel.queryservices;

public interface IPaymentQueryService {
    Payment FindPayment(String paymentId);
}

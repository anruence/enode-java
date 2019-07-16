package com.microsoft.conference.registration.readmodel.queryservices;

public interface IPaymentQueryService {
    Payment findPayment(String paymentId);
}

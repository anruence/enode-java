package com.microsoft.conference.registration.readmodel.service.impl;

import com.microsoft.conference.registration.readmodel.service.IPaymentQueryService;
import com.microsoft.conference.registration.readmodel.service.Payment;

public class PaymentQueryService implements IPaymentQueryService {
    @Override
    public Payment findPayment(String paymentId) {
        return null;
    }
//        public Payment FindPayment(String paymentId)
//        {
//            using (var connection = GetConnection())
//            {
//                return connection.QueryList<Payment>(new { Id = paymentId }, ConfigSettings.PaymentTable).FirstOrDefault();
//            }
//        }
//
//        private IDbConnection GetConnection()
//        {
//            return new SqlConnection(ConfigSettings.ConferenceConnectionString);
//        }
}

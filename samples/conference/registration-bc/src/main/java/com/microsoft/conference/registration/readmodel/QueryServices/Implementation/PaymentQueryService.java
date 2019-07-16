package com.microsoft.conference.registration.readmodel.QueryServices.Implementation;

import com.microsoft.conference.registration.readmodel.QueryServices.IPaymentQueryService;
import com.microsoft.conference.registration.readmodel.QueryServices.Payment;

public class PaymentQueryService implements IPaymentQueryService {
    @Override
    public Payment FindPayment(String paymentId) {
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

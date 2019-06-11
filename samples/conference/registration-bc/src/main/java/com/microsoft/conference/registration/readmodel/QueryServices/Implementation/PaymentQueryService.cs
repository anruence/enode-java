using System;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using Conference.Common;
using ECommon.Components;
using ECommon.Dapper;

namespace Registration.ReadModel.Implementation
{
    [Component]
    public class PaymentQueryService : IPaymentQueryService
    {
        public Payment FindPayment(Guid paymentId)
        {
            using (var connection = GetConnection())
            {
                return connection.QueryList<Payment>(new { Id = paymentId }, ConfigSettings.PaymentTable).FirstOrDefault();
            }
        }

        private IDbConnection GetConnection()
        {
            return new SqlConnection(ConfigSettings.ConferenceConnectionString);
        }
    }
}
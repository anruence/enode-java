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
    public class OrderQueryService : IOrderQueryService
    {
        public Order FindOrder(Guid orderId)
        {
            using (var connection = GetConnection())
            {
                var order = connection.QueryList<Order>(new { OrderId = orderId }, ConfigSettings.OrderTable).FirstOrDefault();
                if (order != null)
                {
                    order.SetLines(connection.QueryList<OrderLine>(new { OrderId = orderId }, ConfigSettings.OrderLineTable).ToList());
                    return order;
                }
                return null;
            }
        }
        public Guid? LocateOrder(string email, string accessCode)
        {
            using (var connection = GetConnection())
            {
                var order = connection.QueryList<Order>(new { RegistrantEmail = email, AccessCode = accessCode }, ConfigSettings.OrderTable).FirstOrDefault();
                if (order != null)
                {
                    return order.OrderId;
                }
                return null;
            }
        }
        public OrderSeatAssignment[] FindOrderSeatAssignments(Guid orderId)
        {
            using (var connection = GetConnection())
            {
                return connection.QueryList<OrderSeatAssignment>(new { OrderId = orderId }, ConfigSettings.OrderSeatAssignmentsTable).ToArray();
            }
        }

        private IDbConnection GetConnection()
        {
            return new SqlConnection(ConfigSettings.ConferenceConnectionString);
        }
    }
}
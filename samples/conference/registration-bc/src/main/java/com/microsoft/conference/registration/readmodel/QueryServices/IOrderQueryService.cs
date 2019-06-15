using System;

namespace Registration.ReadModel
{
    public interface IOrderQueryService
    {
        Order FindOrder(Guid orderId);
        Guid? LocateOrder(string email, string accessCode);
        OrderSeatAssignment[] FindOrderSeatAssignments(Guid orderId);
    }
}
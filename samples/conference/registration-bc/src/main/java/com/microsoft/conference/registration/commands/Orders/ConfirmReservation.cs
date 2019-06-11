using System;
using ENode.Commanding;

namespace Registration.Commands.Orders
{
    public class ConfirmReservation : Command<Guid>
    {
        public bool IsReservationSuccess { get; set; }

        public ConfirmReservation() { }
        public ConfirmReservation(Guid orderId, bool isReservationSuccess) : base(orderId)
        {
            IsReservationSuccess = isReservationSuccess;
        }
    }
}

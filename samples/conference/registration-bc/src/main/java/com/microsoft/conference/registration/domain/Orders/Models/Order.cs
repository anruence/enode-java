using System;
using System.Collections.Generic;
using System.Linq;
using Conference.Common;
using ECommon.Utilities;
using ENode.Domain;
using Registration.SeatAssigning;

namespace Registration.Orders
{
    public class Order : AggregateRoot<Guid>
    {
        private OrderTotal _total;
        private Guid _conferenceId;
        private OrderStatus _status;
        private Registrant _registrant;
        private string _accessCode;

        public Order(Guid id, Guid conferenceId, IEnumerable<SeatQuantity> seats, IPricingService pricingService) : base(id)
        {
            Ensure.NotEmptyGuid(id, "id");
            Ensure.NotEmptyGuid(conferenceId, "conferenceId");
            Ensure.NotNull(seats, "seats");
            Ensure.NotNull(pricingService, "pricingService");
            if (!seats.Any()) throw new ArgumentException("The seats of order cannot be empty.");

            var orderTotal = pricingService.CalculateTotal(conferenceId, seats);
            ApplyEvent(new OrderPlaced(conferenceId, orderTotal, DateTime.UtcNow.Add(ConfigSettings.ReservationAutoExpiration), ObjectId.GenerateNewStringId()));
        }

        public void AssignRegistrant(string firstName, string lastName, string email)
        {
            ApplyEvent(new OrderRegistrantAssigned(_conferenceId, new Registrant(firstName, lastName, email)));
        }
        public void ConfirmReservation(bool isReservationSuccess)
        {
            if (_status != OrderStatus.Placed)
            {
                throw new InvalidOperationException("Invalid order status:" + _status);
            }
            if (isReservationSuccess)
            {
                ApplyEvent(new OrderReservationConfirmed(_conferenceId, OrderStatus.ReservationSuccess));
            }
            else
            {
                ApplyEvent(new OrderReservationConfirmed(_conferenceId, OrderStatus.ReservationFailed));
            }
        }
        public void ConfirmPayment(bool isPaymentSuccess)
        {
            if (_status != OrderStatus.ReservationSuccess)
            {
                throw new InvalidOperationException("Invalid order status:" + _status);
            }
            if (isPaymentSuccess)
            {
                ApplyEvent(new OrderPaymentConfirmed(_conferenceId, OrderStatus.PaymentSuccess));
            }
            else
            {
                ApplyEvent(new OrderPaymentConfirmed(_conferenceId, OrderStatus.PaymentRejected));
            }
        }
        public void MarkAsSuccess()
        {
            if (_status != OrderStatus.PaymentSuccess)
            {
                throw new InvalidOperationException("Invalid order status:" + _status);
            }
            ApplyEvent(new OrderSuccessed(_conferenceId));
        }
        public void MarkAsExpire()
        {
            if (_status == OrderStatus.ReservationSuccess)
            {
                ApplyEvent(new OrderExpired(_conferenceId));
            }
        }
        public void Close()
        {
            if (_status != OrderStatus.ReservationSuccess && _status != OrderStatus.PaymentRejected)
            {
                throw new InvalidOperationException("Invalid order status:" + _status);
            }
            ApplyEvent(new OrderClosed(_conferenceId));
        }
        public OrderSeatAssignments CreateSeatAssignments()
        {
            if (_status != OrderStatus.Success)
            {
                throw new InvalidOperationException("Cannot create seat assignments for an order that isn't success yet.");
            }
            return new OrderSeatAssignments(_id, _total.Lines);
        }

        private void Handle(OrderPlaced evnt)
        {
            _id = evnt.AggregateRootId;
            _conferenceId = evnt.ConferenceId;
            _total = evnt.OrderTotal;
            _accessCode = evnt.AccessCode;
            _status = OrderStatus.Placed;
        }
        private void Handle(OrderRegistrantAssigned evnt)
        {
            _registrant = evnt.Registrant;
        }
        private void Handle(OrderReservationConfirmed evnt)
        {
            _status = evnt.OrderStatus;
        }
        private void Handle(OrderPaymentConfirmed evnt)
        {
            _status = evnt.OrderStatus;
        }
        private void Handle(OrderSuccessed evnt)
        {
            _status = OrderStatus.Success;
        }
        private void Handle(OrderExpired evnt)
        {
            _status = OrderStatus.Expired;
        }
        private void Handle(OrderClosed evnt)
        {
            _status = OrderStatus.Closed;
        }
    }
    public enum OrderStatus
    {
        Placed = 1,                //订单已生成
        ReservationSuccess = 2,    //位置预定已成功（下单已成功）
        ReservationFailed = 3,     //位置预定已失败（下单失败）
        PaymentSuccess = 4,        //付款已成功
        PaymentRejected = 5,       //付款已拒绝
        Expired = 6,               //订单已过期
        Success = 7,               //交易已成功
        Closed = 8                 //订单已关闭
    }
}

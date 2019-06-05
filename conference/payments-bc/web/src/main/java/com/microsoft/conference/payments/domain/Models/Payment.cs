using System;
using System.Collections.Generic;
using ENode.Domain;

namespace Payments
{
    public class Payment : AggregateRoot<Guid>
    {
        private Guid _orderId;
        private Guid _conferenceId;
        private PaymentState _state;
        private string _description;
        private decimal _totalAmount;
        private IEnumerable<PaymentItem> _items;

        public Payment(Guid id, Guid orderId, Guid conferenceId, string description, decimal totalAmount, IEnumerable<PaymentItem> items) : base(id)
        {
            ApplyEvent(new PaymentInitiated(orderId, conferenceId, description, totalAmount, items));
        }

        public void Complete()
        {
            if (_state != PaymentState.Initiated)
            {
                throw new InvalidOperationException();
            }
            ApplyEvent(new PaymentCompleted(this, _orderId, _conferenceId));
        }
        public void Cancel()
        {
            if (_state != PaymentState.Initiated)
            {
                throw new InvalidOperationException();
            }
            ApplyEvent(new PaymentRejected(_orderId, _conferenceId));
        }

        private void Handle(PaymentInitiated evnt)
        {
            _id = evnt.AggregateRootId;
            _orderId = evnt.OrderId;
            _conferenceId = evnt.ConferenceId;
            _description = evnt.Description;
            _totalAmount = evnt.TotalAmount;
            _state = PaymentState.Initiated;
            _items = evnt.Items;
        }
        private void Handle(PaymentCompleted evnt)
        {
            _state = PaymentState.Completed;
        }
        private void Handle(PaymentRejected evnt)
        {
            _state = PaymentState.Rejected;
        }
    }
    public class PaymentItem
    {
        public PaymentItem(string description, decimal amount)
        {
            this.Id = Guid.NewGuid();
            this.Description = description;
            this.Amount = amount;
        }

        public Guid Id { get; private set; }
        public string Description { get; private set; }
        public decimal Amount { get; private set; }
    }
    public enum PaymentState
    {
        Initiated = 0,
        Completed = 1,
        Rejected = 2
    }
}

package com.microsoft.conference.payments.domain.Models;

import com.microsoft.conference.payments.domain.Events.PaymentCompleted;
import com.microsoft.conference.payments.domain.Events.PaymentInitiated;
import com.microsoft.conference.payments.domain.Events.PaymentRejected;
import org.enodeframework.common.exception.InvalidOperationException;
import org.enodeframework.domain.AggregateRoot;

import java.math.BigDecimal;
import java.util.List;

public class Payment extends AggregateRoot<String> {
    private String _orderId;
    private String _conferenceId;
    private int _state;
    private String _description;
    private BigDecimal _totalAmount;
    private List<PaymentItem> _items;

    public Payment(String id, String orderId, String conferenceId, String description, BigDecimal totalAmount, List<PaymentItem> items) {
        super(id);
        applyEvent(new PaymentInitiated(orderId, conferenceId, description, totalAmount, items));
    }

    public void Complete() {
        if (_state != PaymentState.Initiated) {
            throw new InvalidOperationException();
        }
        applyEvent(new PaymentCompleted(this, _orderId, _conferenceId));
    }

    public void Cancel() {
        if (_state != PaymentState.Initiated) {
            throw new InvalidOperationException();
        }
        applyEvent(new PaymentRejected(_orderId, _conferenceId));
    }

    private void Handle(PaymentInitiated evnt) {
        id = evnt.getAggregateRootId();
        _orderId = evnt.OrderId;
        _conferenceId = evnt.ConferenceId;
        _description = evnt.Description;
        _totalAmount = evnt.TotalAmount;
        _state = PaymentState.Initiated;
        _items = evnt.Items;
    }

    private void Handle(PaymentCompleted evnt) {
        _state = PaymentState.Completed;
    }

    private void Handle(PaymentRejected evnt) {
        _state = PaymentState.Rejected;
    }
}

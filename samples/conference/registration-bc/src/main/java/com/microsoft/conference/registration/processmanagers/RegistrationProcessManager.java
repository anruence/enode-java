package com.microsoft.conference.registration.processmanagers;

import com.microsoft.conference.common.management.commands.CancelSeatReservation;
import com.microsoft.conference.common.management.commands.CommitSeatReservation;
import com.microsoft.conference.common.management.commands.MakeSeatReservation;
import com.microsoft.conference.common.management.commands.SeatReservationItemInfo;
import com.microsoft.conference.common.management.message.SeatInsufficientMessage;
import com.microsoft.conference.common.management.message.SeatsReservationCancelledMessage;
import com.microsoft.conference.common.management.message.SeatsReservationCommittedMessage;
import com.microsoft.conference.common.management.message.SeatsReservedMessage;
import com.microsoft.conference.common.payment.message.PaymentCompletedMessage;
import com.microsoft.conference.common.payment.message.PaymentRejectedMessage;
import com.microsoft.conference.common.registration.commands.orders.CloseOrder;
import com.microsoft.conference.common.registration.commands.orders.ConfirmPayment;
import com.microsoft.conference.common.registration.commands.orders.ConfirmReservation;
import com.microsoft.conference.common.registration.commands.orders.MarkAsSuccess;
import com.microsoft.conference.common.registration.commands.seatassignments.CreateSeatAssignments;
import com.microsoft.conference.registration.domain.orders.Events.OrderExpired;
import com.microsoft.conference.registration.domain.orders.Events.OrderPaymentConfirmed;
import com.microsoft.conference.registration.domain.orders.Events.OrderPlaced;
import com.microsoft.conference.registration.domain.orders.Events.OrderSuccessed;
import com.microsoft.conference.registration.domain.orders.Models.OrderStatus;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.io.Task;

import java.util.stream.Collectors;

/**
 * IMessageHandler<OrderPlaced>,                           //订单创建时发生(Order)
 * <p>
 * IMessageHandler<SeatsReservedMessage>,                  //预扣库存，成功时发生(Conference)
 * IMessageHandler<SeatInsufficientMessage>,               //预扣库存，库存不足时发生(Conference)
 * <p>
 * IMessageHandler<PaymentCompletedMessage>,               //支付成功时发生(Payment)
 * IMessageHandler<PaymentRejectedMessage>,                //支付拒绝时发生(Payment)
 * <p>
 * IMessageHandler<OrderPaymentConfirmed>,                 //确认支付时发生(Order)
 * <p>
 * IMessageHandler<SeatsReservationCommittedMessage>,      //预扣库存提交时发生(Conference)
 * IMessageHandler<SeatsReservationCancelledMessage>,      //预扣库存取消时发生(Conference)
 * <p>
 * IMessageHandler<OrderSuccessed>,                        //订单处理成功时发生(Order)
 * <p>
 * IMessageHandler<OrderExpired>                           //订单过期时(15分钟过期)发生(Order)
 */
public class RegistrationProcessManager {
    private ICommandService _commandService;

    public RegistrationProcessManager(ICommandService commandService) {
        _commandService = commandService;
    }

    public void HandleAsync(OrderPlaced evnt) {
        MakeSeatReservation reservation = new MakeSeatReservation(evnt.ConferenceId);
        reservation.ReservationId = evnt.getAggregateRootId();
        reservation.Seats = evnt.OrderTotal.Lines.stream().map(x -> {
            SeatReservationItemInfo itemInfo = new SeatReservationItemInfo();
            itemInfo.SeatType = x.SeatQuantity.Seat.SeatTypeId;
            itemInfo.Quantity = x.SeatQuantity.Quantity;
            return itemInfo;
        }).collect(Collectors.toList());
        Task.await(_commandService.sendAsync(reservation));
    }

    public void HandleAsync(SeatsReservedMessage message) {
        Task.await(_commandService.sendAsync(new ConfirmReservation(message.ReservationId, true)));
    }

    public void HandleAsync(SeatInsufficientMessage message) {
        Task.await(_commandService.sendAsync(new ConfirmReservation(message.ReservationId, false)));
    }

    public void HandleAsync(PaymentCompletedMessage message) {
        Task.await(_commandService.sendAsync(new ConfirmPayment(message.OrderId, true)));
    }

    public void HandleAsync(PaymentRejectedMessage message) {
        Task.await(_commandService.sendAsync(new ConfirmPayment(message.OrderId, false)));
    }

    public void HandleAsync(OrderPaymentConfirmed evnt) {
        if (evnt.orderStatus == OrderStatus.PaymentSuccess) {
            Task.await(_commandService.sendAsync(new CommitSeatReservation(evnt.ConferenceId, evnt.getAggregateRootId())));
        } else if (evnt.orderStatus == OrderStatus.PaymentRejected) {
            Task.await(_commandService.sendAsync(new CancelSeatReservation(evnt.ConferenceId, evnt.getAggregateRootId())));
        }
    }

    public void HandleAsync(SeatsReservationCommittedMessage message) {
        Task.await(_commandService.sendAsync(new MarkAsSuccess(message.ReservationId)));
    }

    public void HandleAsync(SeatsReservationCancelledMessage message) {
        Task.await(_commandService.sendAsync(new CloseOrder(message.ReservationId)));
    }

    public void HandleAsync(OrderSuccessed evnt) {
        Task.await(_commandService.sendAsync(new CreateSeatAssignments(evnt.getAggregateRootId())));
    }

    public void HandleAsync(OrderExpired evnt) {
        Task.await(_commandService.sendAsync(new CancelSeatReservation(evnt.ConferenceId, evnt.getAggregateRootId())));
    }
}
        

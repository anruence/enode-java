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
    private ICommandService commandService;

    public RegistrationProcessManager(ICommandService commandService) {
        this.commandService = commandService;
    }

    public void handleAsync(OrderPlaced evnt) {
        MakeSeatReservation reservation = new MakeSeatReservation(evnt.conferenceId);
        reservation.reservationId = evnt.getAggregateRootId();
        reservation.seats = evnt.orderTotal.orderLines.stream().map(x -> {
            SeatReservationItemInfo itemInfo = new SeatReservationItemInfo();
            itemInfo.seatType = x.seatQuantity.seatType.seatTypeId;
            itemInfo.quantity = x.seatQuantity.quantity;
            return itemInfo;
        }).collect(Collectors.toList());
        Task.await(commandService.sendAsync(reservation));
    }

    public void handleAsync(SeatsReservedMessage message) {
        Task.await(commandService.sendAsync(new ConfirmReservation(message.reservationId, true)));
    }

    public void handleAsync(SeatInsufficientMessage message) {
        Task.await(commandService.sendAsync(new ConfirmReservation(message.reservationId, false)));
    }

    public void handleAsync(PaymentCompletedMessage message) {
        Task.await(commandService.sendAsync(new ConfirmPayment(message.orderId, true)));
    }

    public void handleAsync(PaymentRejectedMessage message) {
        Task.await(commandService.sendAsync(new ConfirmPayment(message.orderId, false)));
    }

    public void handleAsync(OrderPaymentConfirmed evnt) {
        if (evnt.orderStatus == OrderStatus.PaymentSuccess) {
            Task.await(commandService.sendAsync(new CommitSeatReservation(evnt.conferenceId, evnt.getAggregateRootId())));
        } else if (evnt.orderStatus == OrderStatus.PaymentRejected) {
            Task.await(commandService.sendAsync(new CancelSeatReservation(evnt.conferenceId, evnt.getAggregateRootId())));
        }
    }

    public void handleAsync(SeatsReservationCommittedMessage message) {
        Task.await(commandService.sendAsync(new MarkAsSuccess(message.reservationId)));
    }

    public void handleAsync(SeatsReservationCancelledMessage message) {
        Task.await(commandService.sendAsync(new CloseOrder(message.reservationId)));
    }

    public void handleAsync(OrderSuccessed evnt) {
        Task.await(commandService.sendAsync(new CreateSeatAssignments(evnt.getAggregateRootId())));
    }

    public void handleAsync(OrderExpired evnt) {
        Task.await(commandService.sendAsync(new CancelSeatReservation(evnt.conferenceId, evnt.getAggregateRootId())));
    }
}
        

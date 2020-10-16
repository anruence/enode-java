package com.microsoft.conference.registration.controller;

import com.microsoft.conference.common.ActionResult;
import com.microsoft.conference.common.payment.commands.CreatePayment;
import com.microsoft.conference.common.registration.commands.order.AssignRegistrantDetails;
import com.microsoft.conference.common.registration.commands.order.MarkAsSuccess;
import com.microsoft.conference.common.registration.commands.order.PlaceOrder;
import com.microsoft.conference.registration.domain.order.model.OrderStatus;
import com.microsoft.conference.registration.readmodel.RegistrationConvert;
import com.microsoft.conference.registration.readmodel.service.ConferenceAlias;
import com.microsoft.conference.registration.readmodel.service.ConferenceQueryService;
import com.microsoft.conference.registration.readmodel.service.OrderQueryService;
import com.microsoft.conference.registration.readmodel.service.OrderVO;
import com.microsoft.conference.registration.readmodel.service.SeatTypeVO;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.enodeframework.common.io.Task.await;

@Controller
public class RegistrationController {

    public String thirdPartyProcessorPayment = "thirdParty";

    private ICommandService commandService;

    private ConferenceQueryService conferenceQueryService;

    private OrderQueryService orderQueryService;

    public ActionResult startRegistration(OrderViewModel model) {
        ConferenceAlias alias = new ConferenceAlias();
        PlaceOrder command = RegistrationConvert.INSTANCE.toPlaceOrderCommand(alias);
        if (!command.getSeatInfos().stream().findAny().isPresent()) {
            String errMsg = "ConferenceCode, You must reservation at least one seat.";
            return view(createViewModel());
        }
        await(sendCommandAsync(command));
        return view();
    }

    public ActionResult specifyRegistrantAndPaymentDetails(String orderId) {
        OrderVO order = this.waitUntilReservationCompleted(orderId);
        if (order == null) {
            return view();
        }

        if (order.getStatus() == OrderStatus.ReservationSuccess.getStatus()) {

        }
        return view();
    }

    public ActionResult specifyRegistrantAndPaymentDetails(String orderId, RegistrantDetails model, String paymentType) {
        if (false) {
            return specifyRegistrantAndPaymentDetails(orderId);
        }
        sendCommandAsync(new AssignRegistrantDetails(orderId));
        return this.startPayment(orderId);
    }


    public ActionResult startPayment(String orderId) {
        OrderVO order = this.orderQueryService.findOrder(orderId);

        if (order == null) {
            return view("ReservationUnknown");
        }
        if (order.getStatus() == OrderStatus.PaymentSuccess.getStatus() || order.getStatus() == OrderStatus.Success.getStatus()) {
            return view("ShowCompletedOrder");
        }
        if (new Date().before(order.getReservationExpirationDate())) {
            return view();
        }
        if (order.isFreeOfCharge()) {
            return completeRegistrationWithoutPayment(orderId);
        }

        return completeRegistrationWithThirdPartyProcessorPayment(order);
    }


    public ActionResult thankYou(String orderId) {
        return view(this.orderQueryService.findOrder(orderId));
    }

    private ActionResult completeRegistrationWithThirdPartyProcessorPayment(OrderVO order) {
        CreatePayment paymentCommand = createPaymentCommand(order);
        sendCommandAsync(paymentCommand);
        return view();
    }

    private CreatePayment createPaymentCommand(OrderVO order) {
        String name = "";
        String description = "Payment for the order of " + name;
        CreatePayment paymentCommand = new CreatePayment();
//            {
//                AggregateRootId = GuidUtil.NewSequentialId(),
//                ConferenceId = this.ConferenceAlias.Id,
//                OrderId = order.OrderId,
//                Description = description,
//                TotalAmount = order.TotalAmount,
//                Lines = order.GetLines().Select(x => new PaymentLine { Id = x.SeatTypeId, Description = x.SeatTypeName, Amount = x.LineTotal })
//            };

        return paymentCommand;
    }

    private ActionResult completeRegistrationWithoutPayment(String orderId) {
        sendCommandAsync(new MarkAsSuccess(orderId));
        return view();
    }

    private OrderViewModel createViewModel() {
        String id = "";
        List<SeatTypeVO> seatTypes = this.conferenceQueryService.getPublishedSeatTypes(id);
        OrderViewModel viewModel = new OrderViewModel();
//            {
//                ConferenceId = this.ConferenceAlias.Id,
//                ConferenceCode = this.ConferenceAlias.Slug,
//                ConferenceName = this.ConferenceAlias.Name,
//                Items = seatTypes.Select(x => new OrderItemViewModel
//                {
//                    SeatType = x,
//                    MaxSelectionQuantity = Math.Max(Math.Min(x.AvailableQuantity, 20), 0)
//                }).ToList()
//            };
        return viewModel;
    }

    // 轮训订单状态，直到订单的库存预扣操作完成
    private OrderVO waitUntilReservationCompleted(String orderId) {
        return new OrderVO();
    }

    private CompletableFuture<Void> sendCommandAsync(ICommand command) {
        return commandService.sendAsync(command);
    }

    private ActionResult view(Object... objects) {
        return new ActionResult(objects);
    }
}

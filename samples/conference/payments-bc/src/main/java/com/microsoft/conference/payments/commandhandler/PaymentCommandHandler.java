package com.microsoft.conference.payments.commandhandler;

import com.microsoft.conference.common.payment.commands.CancelPayment;
import com.microsoft.conference.common.payment.commands.CompletePayment;
import com.microsoft.conference.common.payment.commands.CreatePayment;
import com.microsoft.conference.payments.domain.model.Payment;
import com.microsoft.conference.payments.domain.model.PaymentItem;
import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;

import java.util.List;
import java.util.stream.Collectors;

import static org.enodeframework.common.io.Task.await;

/**
 * ICommandHandler<CreatePayment>,
 * ICommandHandler<CompletePayment>,
 * ICommandHandler<CancelPayment>
 */
@Command
public class PaymentCommandHandler {

    @Subscribe
    public void handleAsync(ICommandContext context, CreatePayment command) {
        List<PaymentItem> paymentItemList = command.lines.stream().map(x -> new PaymentItem(x.description, x.amount)).collect(Collectors.toList());
        context.addAsync(new Payment(
                command.getAggregateRootId(),
                command.orderId,
                command.conferenceId,
                command.description,
                command.totalAmount,
                paymentItemList));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, CompletePayment command) {
        Payment payment = await(context.getAsync(command.getAggregateRootId(), Payment.class));
        payment.complete();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, CancelPayment command) {
        Payment payment = await(context.getAsync(command.getAggregateRootId(), Payment.class));
        payment.cancel();
    }
}
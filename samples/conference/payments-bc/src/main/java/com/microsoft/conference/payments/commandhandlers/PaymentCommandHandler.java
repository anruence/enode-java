package com.microsoft.conference.payments.commandhandlers;

import com.enodeframework.annotation.Command;
import com.enodeframework.commanding.ICommandContext;
import com.microsoft.conference.common.payment.commands.CancelPayment;
import com.microsoft.conference.common.payment.commands.CompletePayment;
import com.microsoft.conference.common.payment.commands.CreatePayment;
import com.microsoft.conference.payments.domain.Models.Payment;
import com.microsoft.conference.payments.domain.Models.PaymentItem;

import java.util.List;
import java.util.stream.Collectors;

import static com.enodeframework.common.io.Task.await;

/**
 * ICommandHandler<CreatePayment>,
 * ICommandHandler<CompletePayment>,
 * ICommandHandler<CancelPayment>
 */
@Command
public class PaymentCommandHandler {
    public void HandleAsync(ICommandContext context, CreatePayment command) {
        List<PaymentItem> paymentItemList = command.Lines.stream().map(x -> new PaymentItem(x.Description, x.Amount)).collect(Collectors.toList());
        context.addAsync(new Payment(
                command.getAggregateRootId(),
                command.OrderId,
                command.ConferenceId,
                command.Description,
                command.TotalAmount,
                paymentItemList));
    }

    public void HandleAsync(ICommandContext context, CompletePayment command) {
        Payment payment = await(context.getAsync(command.getAggregateRootId(), Payment.class));
        payment.Complete();
    }

    public void HandleAsync(ICommandContext context, CancelPayment command) {
        Payment payment = await(context.getAsync(command.getAggregateRootId(), Payment.class));
        payment.Cancel();
    }
}
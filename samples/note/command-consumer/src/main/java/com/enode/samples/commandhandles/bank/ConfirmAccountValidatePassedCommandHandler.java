package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.ConfirmAccountValidatePassedCommand;
import com.enode.samples.domain.bank.transfertransaction.TransferTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ConfirmAccountValidatePassedCommandHandler implements ICommandHandler<ConfirmAccountValidatePassedCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmAccountValidatePassedCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        future.thenAccept(transaction -> transaction.ConfirmAccountValidatePassed(command.AccountId));
        return future;
    }
}
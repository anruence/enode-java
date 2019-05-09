package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.StartTransferTransactionCommand;
import com.enode.samples.domain.bank.transfertransaction.TransferTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 开始转账交易
 */
@Component
public class StartTransferTransactionCommandHandle implements ICommandHandler<StartTransferTransactionCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, StartTransferTransactionCommand command) {
        return context.addAsync(new TransferTransaction(command.getAggregateRootId(), command.TransactionInfo));
    }
}
package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.CreateAccountCommand;
import com.enode.samples.domain.bank.bankaccount.BankAccount;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/// <summary>银行账户相关命令处理
/// </summary>
@Component
public class CreateAccountCommandHandler implements ICommandHandler<CreateAccountCommand>                   //开户
{
    @Override
    public CompletableFuture handleAsync(ICommandContext context, CreateAccountCommand command) {
        return context.addAsync(new BankAccount(command.getAggregateRootId(), command.Owner));
    }
}

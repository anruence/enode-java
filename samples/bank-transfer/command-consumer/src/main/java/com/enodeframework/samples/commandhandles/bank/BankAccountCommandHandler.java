package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.annotation.Command;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Task;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.samples.applicationmessages.AccountValidateFailedMessage;
import com.enodeframework.samples.applicationmessages.AccountValidatePassedMessage;
import com.enodeframework.samples.commands.bank.AddTransactionPreparationCommand;
import com.enodeframework.samples.commands.bank.CommitTransactionPreparationCommand;
import com.enodeframework.samples.commands.bank.CreateAccountCommand;
import com.enodeframework.samples.commands.bank.ValidateAccountCommand;
import com.enodeframework.samples.domain.bank.bankaccount.BankAccount;

import java.util.concurrent.CompletableFuture;

/**
 * 银行账户相关命令处理
 * ICommandHandler<CreateAccountCommand>,                       //开户
 * ICommandAsyncHandler<ValidateAccountCommand>,                //验证账户是否合法
 * ICommandHandler<AddTransactionPreparationCommand>,           //添加预操作
 * ICommandHandler<CommitTransactionPreparationCommand>         //提交预操作
 */
@Command
public class BankAccountCommandHandler {
    /**
     * 开户
     */
    @Subscribe
    public void handleAsync(ICommandContext context, CreateAccountCommand command) {
        context.addAsync(new BankAccount(command.getAggregateRootId(), command.Owner));
    }

    /**
     * 添加预操作
     */
    @Subscribe
    public void handleAsync(ICommandContext context, AddTransactionPreparationCommand command) {
        CompletableFuture<BankAccount> future = context.getAsync(command.getAggregateRootId(), BankAccount.class);
        BankAccount account = Task.get(future);
        account.AddTransactionPreparation(command.TransactionId, command.TransactionType, command.PreparationType, command.Amount);
    }


    /**
     * 验证账户是否合法
     *
     * @param command
     * @return
     */
    @Subscribe
    public AsyncTaskResult<IApplicationMessage> handleAsync(ValidateAccountCommand command) {
        IApplicationMessage applicationMessage = new AccountValidatePassedMessage(command.getAggregateRootId(), command.TransactionId);
        //此处应该会调用外部接口验证账号是否合法，这里仅仅简单通过账号是否以INVALID字符串开头来判断是否合法；根据账号的合法性，返回不同的应用层消息
        if (command.getAggregateRootId().startsWith("INVALID")) {
            applicationMessage = new AccountValidateFailedMessage(command.getAggregateRootId(), command.TransactionId, "账户不合法.");
        }
        return new AsyncTaskResult<>(AsyncTaskStatus.Success, applicationMessage);
    }

    /**
     * 提交预操作
     *
     * @param context
     * @param command
     * @return
     */
    @Subscribe
    public void handleAsync(ICommandContext context, CommitTransactionPreparationCommand command) {
        CompletableFuture<BankAccount> future = context.getAsync(command.getAggregateRootId(), BankAccount.class);
        BankAccount account = Task.get(future);
        account.CommitTransactionPreparation(command.TransactionId);
    }
}

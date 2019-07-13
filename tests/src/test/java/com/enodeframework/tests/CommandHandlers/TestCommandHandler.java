package com.enodeframework.tests.CommandHandlers;

import com.enodeframework.annotation.Command;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Task;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.tests.Commands.*;
import com.enodeframework.tests.Domain.InheritTestAggregate;
import com.enodeframework.tests.Domain.TestAggregate;

import static com.enodeframework.common.io.Task.await;

@Command
public class TestCommandHandler {

    @Subscribe
    public void handleAsync(ICommandContext context, CreateTestAggregateCommand command) {
        if (command.SleepMilliseconds > 0) {
            Task.sleep(command.SleepMilliseconds);
        }
        context.add(new TestAggregate(command.getAggregateRootId(), command.Title));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ChangeTestAggregateTitleCommand command) {
        TestAggregate testAggregate = await(context.getAsync(command.aggregateRootId, TestAggregate.class));
        testAggregate.ChangeTitle(command.Title);
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, CreateInheritTestAggregateCommand command) {
        context.add(new InheritTestAggregate(command.getAggregateRootId(), command.Title));

    }

    @Subscribe
    public void HandleAsync(ICommandContext context, ChangeInheritTestAggregateTitleCommand command) {
        InheritTestAggregate testAggregate = await(context.getAsync(command.getAggregateRootId(), InheritTestAggregate.class));
        testAggregate.ChangeMyTitle(command.Title);
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, ChangeNothingCommand command) {

    }

    @Subscribe
    public void HandleAsync(ICommandContext context, SetResultCommand command) {
        context.add(new TestAggregate(command.getAggregateRootId(), ""));
        context.setResult(command.Result);
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, ChangeMultipleAggregatesCommand command) {
        TestAggregate testAggregate1 = await(context.getAsync(command.getAggregateRootId1(), TestAggregate.class));
        TestAggregate testAggregate2 = await(context.getAsync(command.getAggregateRootId2(), TestAggregate.class));
        testAggregate1.TestEvents();
        testAggregate2.TestEvents();
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, ThrowExceptionCommand command) throws Exception {
        throw new Exception("CommandException");
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, AggregateThrowExceptionCommand command) throws Exception {
        TestAggregate testAggregate = await(context.getAsync(command.aggregateRootId, TestAggregate.class));
        testAggregate.ThrowException(command.PublishableException);
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, TestEventPriorityCommand command) {
        TestAggregate testAggregate = await(context.getAsync(command.aggregateRootId, TestAggregate.class));
        testAggregate.TestEvents();
    }

    @Subscribe
    public void HandleAsync1(ICommandContext context, TwoHandlersCommand command) {

    }

    @Subscribe
    public void HandleAsync2(ICommandContext context, TwoHandlersCommand command) {

    }

    @Subscribe
    public void HandleAsync(ICommandContext context, BaseCommand command) {
        context.setResult("ResultFromBaseCommand");
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, ChildCommand command) {
        context.setResult("ResultFromChildCommand");
    }

    @Subscribe
    public AsyncTaskResult<IApplicationMessage> HandleAsync(TwoAsyncHandlersCommand command) {
        return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success);
    }

    @Subscribe
    public AsyncTaskResult<IApplicationMessage> HandleAsync(NotCheckAsyncHandlerExistCommand command) {
        return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success);
    }

    @Subscribe
    public AsyncTaskResult<IApplicationMessage> HandleAsync(NotCheckAsyncHandlerExistWithResultCommand command) {
        return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success, new TestApplicationMessage(command.getAggregateRootId()));
    }

    @Subscribe
    public AsyncTaskResult<IApplicationMessage> HandleAsync(AsyncHandlerBaseCommand command) {
        return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success);
    }

    @Subscribe
    public AsyncTaskResult<IApplicationMessage> HandleAsync(AsyncHandlerChildCommand command) {
        return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success);
    }
}
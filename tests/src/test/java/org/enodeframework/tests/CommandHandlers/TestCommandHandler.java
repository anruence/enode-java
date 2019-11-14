package org.enodeframework.tests.CommandHandlers;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.common.io.Task;
import org.enodeframework.tests.Commands.AggregateThrowExceptionCommand;
import org.enodeframework.tests.Commands.AsyncHandlerBaseCommand;
import org.enodeframework.tests.Commands.AsyncHandlerChildCommand;
import org.enodeframework.tests.Commands.BaseCommand;
import org.enodeframework.tests.Commands.ChangeInheritTestAggregateTitleCommand;
import org.enodeframework.tests.Commands.ChangeMultipleAggregatesCommand;
import org.enodeframework.tests.Commands.ChangeNothingCommand;
import org.enodeframework.tests.Commands.ChangeTestAggregateTitleCommand;
import org.enodeframework.tests.Commands.ChildCommand;
import org.enodeframework.tests.Commands.CreateInheritTestAggregateCommand;
import org.enodeframework.tests.Commands.CreateTestAggregateCommand;
import org.enodeframework.tests.Commands.NotCheckAsyncHandlerExistCommand;
import org.enodeframework.tests.Commands.NotCheckAsyncHandlerExistWithResultCommand;
import org.enodeframework.tests.Commands.SetResultCommand;
import org.enodeframework.tests.Commands.TestEventPriorityCommand;
import org.enodeframework.tests.Commands.ThrowExceptionCommand;
import org.enodeframework.tests.Commands.TwoHandlersCommand;
import org.enodeframework.tests.Domain.InheritTestAggregate;
import org.enodeframework.tests.Domain.TestAggregate;

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
        TestAggregate testAggregate = Task.await(context.getAsync(command.aggregateRootId, TestAggregate.class));
        testAggregate.ChangeTitle(command.Title);
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, CreateInheritTestAggregateCommand command) {
        context.add(new InheritTestAggregate(command.getAggregateRootId(), command.Title));
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, ChangeInheritTestAggregateTitleCommand command) {
        InheritTestAggregate testAggregate = Task.await(context.getAsync(command.getAggregateRootId(), InheritTestAggregate.class));
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
        TestAggregate testAggregate1 = Task.await(context.getAsync(command.getAggregateRootId1(), TestAggregate.class));
        TestAggregate testAggregate2 = Task.await(context.getAsync(command.getAggregateRootId2(), TestAggregate.class));
        testAggregate1.TestEvents();
        testAggregate2.TestEvents();
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, ThrowExceptionCommand command) throws Exception {
        throw new Exception("CommandException");
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, AggregateThrowExceptionCommand command) throws Exception {
        TestAggregate testAggregate = Task.await(context.getAsync(command.aggregateRootId, TestAggregate.class));
        testAggregate.ThrowException(command.PublishableException);
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, TestEventPriorityCommand command) {
        TestAggregate testAggregate = Task.await(context.getAsync(command.aggregateRootId, TestAggregate.class));
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
    public void HandleAsync(ICommandContext context, NotCheckAsyncHandlerExistCommand command) {

    }

    @Subscribe
    public void HandleAsync(ICommandContext context, NotCheckAsyncHandlerExistWithResultCommand command) {
        context.setApplicationMessage(new TestApplicationMessage(command.getAggregateRootId()));
    }

    @Subscribe
    public void HandleAsync(ICommandContext context, AsyncHandlerBaseCommand command) {

    }

    @Subscribe
    public void HandleAsync(ICommandContext context, AsyncHandlerChildCommand command) {

    }
}
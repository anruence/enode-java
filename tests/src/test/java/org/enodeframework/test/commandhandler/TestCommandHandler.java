package org.enodeframework.test.commandhandler;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.common.io.Task;
import org.enodeframework.domain.IMemoryCache;
import org.enodeframework.test.command.AggregateThrowExceptionCommand;
import org.enodeframework.test.command.AsyncHandlerBaseCommand;
import org.enodeframework.test.command.AsyncHandlerChildCommand;
import org.enodeframework.test.command.BaseCommand;
import org.enodeframework.test.command.ChangeInheritTestAggregateTitleCommand;
import org.enodeframework.test.command.ChangeMultipleAggregatesCommand;
import org.enodeframework.test.command.ChangeNothingCommand;
import org.enodeframework.test.command.ChangeTestAggregateTitleCommand;
import org.enodeframework.test.command.ChangeTestAggregateTitleWhenDirtyCommand;
import org.enodeframework.test.command.ChildCommand;
import org.enodeframework.test.command.CreateInheritTestAggregateCommand;
import org.enodeframework.test.command.CreateTestAggregateCommand;
import org.enodeframework.test.command.NotCheckAsyncHandlerExistCommand;
import org.enodeframework.test.command.NotCheckAsyncHandlerExistWithResultCommand;
import org.enodeframework.test.command.SetResultCommand;
import org.enodeframework.test.command.TestEventPriorityCommand;
import org.enodeframework.test.command.ThrowExceptionCommand;
import org.enodeframework.test.command.TwoHandlersCommand;
import org.enodeframework.test.domain.InheritTestAggregate;
import org.enodeframework.test.domain.TestAggregate;

@Command
public class TestCommandHandler {

    @Subscribe
    public void handleAsync(ICommandContext context, ChangeTestAggregateTitleWhenDirtyCommand command) {
        TestAggregate testAggregate = context.getAsync(command.getAggregateRootId(), TestAggregate.class).join();
        if (command.isFirstExecute()) {
            ObjectContainer.resolve(IMemoryCache.class).refreshAggregateFromEventStoreAsync(TestAggregate.class.getName(), command.getAggregateRootId());
        }
        testAggregate.changeTitle(command.getTitle());
        command.setFirstExecute(false);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, CreateTestAggregateCommand command) {
        if (command.sleepMilliseconds > 0) {
            Task.sleep(command.sleepMilliseconds);
        }
        context.add(new TestAggregate(command.getAggregateRootId(), command.title));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ChangeTestAggregateTitleCommand command) {
        TestAggregate testAggregate = Task.await(context.getAsync(command.aggregateRootId, TestAggregate.class));
        testAggregate.changeTitle(command.title);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, CreateInheritTestAggregateCommand command) {
        context.add(new InheritTestAggregate(command.getAggregateRootId(), command.Title));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ChangeInheritTestAggregateTitleCommand command) {
        InheritTestAggregate testAggregate = Task.await(context.getAsync(command.getAggregateRootId(), InheritTestAggregate.class));
        testAggregate.changeMyTitle(command.Title);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ChangeNothingCommand command) {
    }

    @Subscribe
    public void handleAsync(ICommandContext context, SetResultCommand command) {
        context.add(new TestAggregate(command.getAggregateRootId(), ""));
        context.setResult(command.Result);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ChangeMultipleAggregatesCommand command) {
        TestAggregate testAggregate1 = Task.await(context.getAsync(command.getAggregateRootId1(), TestAggregate.class));
        TestAggregate testAggregate2 = Task.await(context.getAsync(command.getAggregateRootId2(), TestAggregate.class));
        testAggregate1.testEvents();
        testAggregate2.testEvents();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ThrowExceptionCommand command) throws Exception {
        throw new Exception("CommandException");
    }

    @Subscribe
    public void handleAsync(ICommandContext context, AggregateThrowExceptionCommand command) throws Exception {
        TestAggregate testAggregate = Task.await(context.getAsync(command.aggregateRootId, TestAggregate.class));
        testAggregate.ThrowException(command.publishableException);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, TestEventPriorityCommand command) {
        TestAggregate testAggregate = Task.await(context.getAsync(command.aggregateRootId, TestAggregate.class));
        testAggregate.testEvents();
    }

    @Subscribe
    public void handleAsync1(ICommandContext context, TwoHandlersCommand command) {
    }

    @Subscribe
    public void handleAsync2(ICommandContext context, TwoHandlersCommand command) {
    }

    @Subscribe
    public void handleAsync(ICommandContext context, BaseCommand command) {
        context.setResult("ResultFromBaseCommand");
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ChildCommand command) {
        context.setResult("ResultFromChildCommand");
    }

    @Subscribe
    public void handleAsync(ICommandContext context, NotCheckAsyncHandlerExistCommand command) {

    }

    @Subscribe
    public void handleAsync(ICommandContext context, NotCheckAsyncHandlerExistWithResultCommand command) {
        context.setApplicationMessage(new TestApplicationMessage(command.getAggregateRootId()));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, AsyncHandlerBaseCommand command) {

    }

    @Subscribe
    public void handleAsync(ICommandContext context, AsyncHandlerChildCommand command) {

    }
}
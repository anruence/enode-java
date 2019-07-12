package com.enodeframework.tests.TestClasses;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.commanding.CommandStatus;
import com.enodeframework.commanding.ICommand;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Task;
import com.enodeframework.common.threading.ManualResetEvent;
import com.enodeframework.common.utilities.ObjectId;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.eventing.EventAppendResult;
import com.enodeframework.eventing.impl.InMemoryEventStore;
import com.enodeframework.infrastructure.ProcessingDomainEventStreamMessage;
import com.enodeframework.tests.Commands.AggregateThrowExceptionCommand;
import com.enodeframework.tests.Commands.AsyncHandlerBaseCommand;
import com.enodeframework.tests.Commands.AsyncHandlerChildCommand;
import com.enodeframework.tests.Commands.AsyncHandlerCommand;
import com.enodeframework.tests.Commands.BaseCommand;
import com.enodeframework.tests.Commands.ChangeInheritTestAggregateTitleCommand;
import com.enodeframework.tests.Commands.ChangeMultipleAggregatesCommand;
import com.enodeframework.tests.Commands.ChangeNothingCommand;
import com.enodeframework.tests.Commands.ChangeTestAggregateTitleCommand;
import com.enodeframework.tests.Commands.ChildCommand;
import com.enodeframework.tests.Commands.CreateInheritTestAggregateCommand;
import com.enodeframework.tests.Commands.CreateTestAggregateCommand;
import com.enodeframework.tests.Commands.NoHandlerCommand;
import com.enodeframework.tests.Commands.NotCheckAsyncHandlerExistWithResultCommand;
import com.enodeframework.tests.Commands.SetResultCommand;
import com.enodeframework.tests.Commands.TestEventPriorityCommand;
import com.enodeframework.tests.Commands.ThrowExceptionCommand;
import com.enodeframework.tests.Commands.TwoAsyncHandlersCommand;
import com.enodeframework.tests.Commands.TwoHandlersCommand;
import com.enodeframework.tests.Domain.InheritTestAggregate;
import com.enodeframework.tests.Domain.TestAggregate;
import com.enodeframework.tests.Domain.TestAggregateCreated;
import com.enodeframework.tests.Domain.TestAggregateTitleChanged;
import com.enodeframework.tests.EventHandlers.Handler1;
import com.enodeframework.tests.EventHandlers.Handler121;
import com.enodeframework.tests.EventHandlers.Handler122;
import com.enodeframework.tests.EventHandlers.Handler123;
import com.enodeframework.tests.EventHandlers.Handler1231;
import com.enodeframework.tests.EventHandlers.Handler1232;
import com.enodeframework.tests.EventHandlers.Handler1233;
import com.enodeframework.tests.EventHandlers.Handler2;
import com.enodeframework.tests.EventHandlers.Handler3;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.enodeframework.common.io.Task.await;

public class CommandAndEventServiceTest extends AbstractTest {

    public static ConcurrentHashMap<Integer, List<String>> HandlerTypes = new ConcurrentHashMap<>();

    private static Logger _logger = LoggerFactory.getLogger(CommandAndEventServiceTest.class);

    @Autowired
    private InMemoryEventStore _eventStore;

    @Test
    public void create_and_update_aggregate_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.version());

        //执行修改聚合根的命令
        ChangeTestAggregateTitleCommand command2 = new ChangeTestAggregateTitleCommand();
        command2.aggregateRootId = aggregateId;
        command2.setTitle("Changed Note");
        asyncResult = Task.get(_commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.version());
    }

    @Test
    public void create_and_update_inherit_aggregate_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateInheritTestAggregateCommand command = new CreateInheritTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");

        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        InheritTestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, InheritTestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.version());
        asyncResult = Task.get(_commandService.executeAsync(command));

        //执行修改聚合根的命令
        ChangeInheritTestAggregateTitleCommand command2 = new ChangeInheritTestAggregateTitleCommand();
        command2.aggregateRootId = aggregateId;
        command2.setTitle("Changed Note");

        asyncResult = Task.get(_commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        note = Task.get(_memoryCache.getAsync(aggregateId, InheritTestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.version());
    }

    @Test
    public void command_sync_execute_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        command.setSleepMilliseconds(3000);

        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> commandResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getData().getStatus());
        TestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.version());

        //执行修改聚合根的命令
        ChangeTestAggregateTitleCommand command2 = new ChangeTestAggregateTitleCommand();
        command2.aggregateRootId = aggregateId;
        command2.setTitle("Changed Note");
        commandResult = Task.get(_commandService.executeAsync(command2));
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getData().getStatus());
        note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.version());
    }

    @Test
    public void duplicate_create_aggregate_command_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");

        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.version());

        //用同一个命令再次执行创建聚合根的命令
        asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.version());

        //用另一个命令再次执行创建相同聚合根的命令
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.version());
    }

    @Test

    public void duplicate_update_aggregate_command_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command1 = new CreateTestAggregateCommand();
        command1.aggregateRootId = aggregateId;
        command1.setTitle("Sample Note");

        //先创建一个聚合根
        CommandStatus status = Task.get(_commandService.executeAsync(command1)).getData().getStatus();
        Assert.assertEquals(CommandStatus.Success, status);

        ChangeTestAggregateTitleCommand command2 = new ChangeTestAggregateTitleCommand();
        command2.aggregateRootId = aggregateId;
        command2.setTitle("Changed Note");

        //执行修改聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command2, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.version());

        //在重复执行该命令
        asyncResult = Task.get(_commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.version());
    }

    @Test

    public void create_and_concurrent_update_aggregate_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");

        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.version());

        //并发执行修改聚合根的命令
        int totalCount = 100;
        int finishedCount = 0;
        ManualResetEvent waitHandle = new ManualResetEvent(false);
        for (int i = 0; i < totalCount; i++) {
            ChangeTestAggregateTitleCommand updateCommand = new ChangeTestAggregateTitleCommand();
            command.aggregateRootId = aggregateId;
            command.setTitle("Changed Note");
            _commandService.executeAsync(updateCommand).thenAccept(result ->
            {
                Assert.assertNotNull(result);
                Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
                Assert.assertNotNull(result.getData());
                Assert.assertEquals(CommandStatus.Success, result.getData().getStatus());

                int current = new AtomicInteger(finishedCount).incrementAndGet();
                if (current == totalCount) {
                    TestAggregate note1 = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
                    Assert.assertNotNull(note1);
                    Assert.assertEquals("Changed Note", note1.getTitle());
                    Assert.assertEquals(totalCount + 1, ((IAggregateRoot) note1).version());
                    waitHandle.set();
                }
            });
        }
        waitHandle.waitOne();
    }

    @Test

    public void change_nothing_test() {
        ChangeNothingCommand command = new ChangeNothingCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.NothingChanged, commandResult.getStatus());
    }

    @Test

    public void set_result_command_test() {
        SetResultCommand command = new SetResultCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        command.setResult("CommandResult");
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        Assert.assertEquals("CommandResult", commandResult.getResult());
    }

    @Test
    public void change_multiple_aggregates_test() {
        CreateTestAggregateCommand command1 = new CreateTestAggregateCommand();
        command1.setId(ObjectId.generateNewStringId());
        command1.setTitle("Sample Note1");
        await(_commandService.executeAsync(command1));
        CreateTestAggregateCommand command2 = new CreateTestAggregateCommand();
        command2.aggregateRootId = ObjectId.generateNewStringId();
        command2.setTitle("Sample Note2");
        await(_commandService.executeAsync(command2));
        ChangeMultipleAggregatesCommand command3 = new ChangeMultipleAggregatesCommand();
        command3.setId(ObjectId.generateNewStringId());
        command3.setAggregateRootId1(command1.getAggregateRootId());
        command3.setAggregateRootId2(command2.getAggregateRootId());
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command3));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
    }

    @Test

    public void no_handler_command_test() {
        NoHandlerCommand command = new NoHandlerCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
    }

    @Test

    public void two_handlers_command_test() {
        TwoHandlersCommand command = new TwoHandlersCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
    }

    @Test

    public void handler_throw_exception_command_test() {
        ThrowExceptionCommand command = new ThrowExceptionCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
    }

    @Test

    public void aggregate_throw_exception_command_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");

        await(_commandService.executeAsync(command));

        AggregateThrowExceptionCommand command1 = new AggregateThrowExceptionCommand();
        command1.aggregateRootId = aggregateId;
        command1.setPublishableException(false);
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());

        AggregateThrowExceptionCommand command2 = new AggregateThrowExceptionCommand();
        command2.aggregateRootId = aggregateId;
        command2.setPublishableException(true);

        asyncResult = Task.get(_commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
    }

    @Test

    public void command_inheritance_test() {
        BaseCommand command = new BaseCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.NothingChanged, commandResult.getStatus());
        Assert.assertEquals("ResultFromBaseCommand", commandResult.getResult());

        command = new ChildCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.NothingChanged, commandResult.getStatus());
        Assert.assertEquals("ResultFromChildCommand", commandResult.getResult());
    }

    // Command Tests

    @Test
    public void async_command_handler_test() {
        AsyncHandlerCommand command = new AsyncHandlerCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
    }

    @Test
    public void async_command_handler_throw_exception_test() {
        AsyncHandlerCommand command = new AsyncHandlerCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        command.setShouldThrowException(true);
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());

        AsyncHandlerCommand command1 = new AsyncHandlerCommand();
        command1.setId(ObjectId.generateNewStringId());
        command1.setShouldThrowIOException(true);
        asyncResult = Task.get(_commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
    }

    @Test

    public void async_command_two_handlers_test() {
        TwoAsyncHandlersCommand command = new TwoAsyncHandlersCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
    }

    @Test

    public void duplicate_async_command_test() {
        AsyncHandlerCommand command = new AsyncHandlerCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());

        asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
    }

    @Test

    public void duplicate_async_command_with_application_message_test() {
        AsyncHandlerCommand command = new AsyncHandlerCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setShouldGenerateApplicationMessage(true);
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());

        asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
    }

    @Test

    public void duplicate_async_command_not_check_handler_exist_with_result_test() {
        NotCheckAsyncHandlerExistWithResultCommand command = new NotCheckAsyncHandlerExistWithResultCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());

        asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
    }

    @Test

    public void async_command_inheritance_test() {
        AsyncHandlerBaseCommand command = new AsyncHandlerBaseCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());

        command = new AsyncHandlerChildCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
    }

    /**
     * region Event
     * Service Tests
     */

    @Test

    public void create_concurrent_conflict_and_then_update_many_times_test() {
        String aggregateId = ObjectId.generateNewStringId();
        String commandId = ObjectId.generateNewStringId();

        TestAggregateCreated aggregateCreated = new TestAggregateCreated("Note Title");
        aggregateCreated.setVersion(1);
        aggregateCreated.setAggregateRootId(aggregateId);
        //往EventStore直接插入事件，用于模拟并发冲突的情况
        DomainEventStream eventStream = new DomainEventStream(
                commandId,
                aggregateId,
                TestAggregate.class.getName(),
                1,
                new Date(),
                Lists.newArrayList(aggregateCreated),
                null);
        AsyncTaskResult<EventAppendResult> result = Task.get(_eventStore.appendAsync(eventStream));
        Assert.assertNotNull(result);
        Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
        Assert.assertEquals(EventAppendResult.Success, result.getData());
        _logger.info("----create_concurrent_conflict_and_then_update_many_times_test, _eventStore.appendAsync success");

        AsyncTaskResult result2 = Task.get(_publishedVersionStore.updatePublishedVersionAsync("DefaultEventProcessor", TestAggregate.class.getName(), aggregateId, 1));
        Assert.assertNotNull(result2);
        Assert.assertEquals(AsyncTaskStatus.Success, result2.getStatus());
        _logger.info("----create_concurrent_conflict_and_then_update_many_times_test, UpdatePublishedversion()Async success");

        //执行创建聚合根的命令
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = (commandId);
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        _logger.info("----create_concurrent_conflict_and_then_update_many_times_test, _commandService.executeAsync create success");

        List<ICommand> commandList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ChangeTestAggregateTitleCommand command1 = new ChangeTestAggregateTitleCommand();
            command1.aggregateRootId = aggregateId;
            command1.setTitle("Changed Note Title");
            commandList.add(command1);
        }

        ManualResetEvent waitHandle = new ManualResetEvent(false);
        long count = 0;
        for (ICommand updateCommand : commandList) {
            _commandService.executeAsync(updateCommand).thenAccept(t ->
            {
                Assert.assertNotNull(t);
                Assert.assertEquals(AsyncTaskStatus.Success, t.getStatus());
                CommandResult updateCommandResult = t.getData();
                Assert.assertNotNull(updateCommandResult);
                Assert.assertEquals(CommandStatus.Success, updateCommandResult.getStatus());
                long totalCount = new AtomicLong(count).incrementAndGet();
                _logger.info("----create_concurrent_conflict_and_then_update_many_times_test, updateCommand finished, count: {}", totalCount);
                if (totalCount == commandList.size()) {
                    waitHandle.set();
                }
            });
        }
        waitHandle.waitOne();
        TestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals(commandList.size() + 1, note.version());
    }

    @Test

    public void create_concurrent_conflict_and_then_update_many_times_test2() {
        String aggregateId = ObjectId.generateNewStringId();
        String commandId = ObjectId.generateNewStringId();

        TestAggregateCreated aggregateCreated = new TestAggregateCreated("Note Title");
        aggregateCreated.setAggregateRootId(aggregateId);
        aggregateCreated.setVersion(1);
        //往EventStore直接插入事件，用于模拟并发冲突的情况
        DomainEventStream eventStream = new DomainEventStream(
                commandId,
                aggregateId,
                TestAggregate.class.getName(),
                1,
                new Date(),
                Lists.newArrayList(aggregateCreated),
                null);
        AsyncTaskResult<EventAppendResult> result = Task.get(_eventStore.appendAsync(eventStream));
        Assert.assertNotNull(result);
        Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
        Assert.assertEquals(EventAppendResult.Success, result.getData());

        AsyncTaskResult result2 = Task.get(_publishedVersionStore.updatePublishedVersionAsync("DefaultEventProcessor", TestAggregate.class.getName(), aggregateId, 1));
        Assert.assertNotNull(result2);
        Assert.assertEquals(AsyncTaskStatus.Success, result2.getStatus());


        List<ICommand> commandList = new ArrayList<>();

        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.id = commandId;
        command.aggregateRootId = commandId;
        command.setTitle("Sample Note");
        commandList.add(command);
        for (int i = 0; i < 50; i++) {
            ChangeTestAggregateTitleCommand command1 = new ChangeTestAggregateTitleCommand();
            command1.aggregateRootId = aggregateId;
            command1.setTitle("Changed Note Title");
            commandList.add(command1);
        }

        ManualResetEvent waitHandle = new ManualResetEvent(false);
        long count = 0L;
        AtomicBoolean createCommandSuccess = new AtomicBoolean(false);
        for (ICommand updateCommand : commandList) {
            _commandService.executeAsync(updateCommand).thenAccept(t ->
            {
                Assert.assertNotNull(t);
                Assert.assertEquals(AsyncTaskStatus.Success, t.getStatus());
                CommandResult commandResult = t.getData();
                Assert.assertNotNull(commandResult);
                Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
                if (commandResult.getCommandId() != commandId) {
                    long totalCount = new AtomicLong(count).incrementAndGet();
                    if (totalCount == commandList.size() - 1) {
                        waitHandle.set();
                    }
                } else {
                    createCommandSuccess.set(true);
                }
            });
        }
        waitHandle.waitOne();
        TestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals(true, createCommandSuccess.get());
        Assert.assertEquals(commandList.size(), note.version());
    }

    @Test
    public void create_concurrent_conflict_and_then_update_many_times_not_enable_batch_insert_test() {
        _eventStore.setSupportBatchAppendEvent(false);
        try {
            create_concurrent_conflict_and_then_update_many_times_test();
            Task.sleep(10);
        } finally {
            _eventStore.setSupportBatchAppendEvent(true);
        }
    }

    @Test

    public void create_concurrent_conflict_and_then_update_many_times_not_enable_batch_insert_test2() {
        _eventStore.setSupportBatchAppendEvent(false);
        try {
            create_concurrent_conflict_and_then_update_many_times_test2();
        } finally {
            _eventStore.setSupportBatchAppendEvent(true);
        }
    }

    @Test

    public void create_concurrent_conflict_not_enable_batch_insert_test() {

        _eventStore.setSupportBatchAppendEvent(false);

        try {
            String aggregateId = ObjectId.generateNewStringId();
            String commandId = ObjectId.generateNewStringId();


            TestAggregateTitleChanged titleChanged = new TestAggregateTitleChanged("Note Title");
            titleChanged.setAggregateRootId(aggregateId);
            titleChanged.setVersion(1);
            //往EventStore直接插入事件，用于模拟并发冲突的情况
            DomainEventStream eventStream = new DomainEventStream(
                    commandId,
                    aggregateId,
                    TestAggregate.class.getName(),
                    1,
                    new Date(),
                    Lists.newArrayList(titleChanged),
                    null);
            AsyncTaskResult result = Task.get(_eventStore.appendAsync(eventStream));
            Assert.assertNotNull(result);
            Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
            Assert.assertEquals(EventAppendResult.Success, result.getData());
            AsyncTaskResult result2 = Task.get(_publishedVersionStore.updatePublishedVersionAsync("DefaultEventProcessor", TestAggregate.class.getName(), aggregateId, 1));
            Assert.assertNotNull(result2);
            Assert.assertEquals(AsyncTaskStatus.Success, result2.getStatus());

            //执行创建聚合根的命令
            CreateTestAggregateCommand command = new CreateTestAggregateCommand();
            command.aggregateRootId = aggregateId;
            command.setTitle("Sample Note");
            AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
            Assert.assertNotNull(asyncResult);
            Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
            CommandResult commandResult = asyncResult.getData();
            Assert.assertNotNull(commandResult);
            Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
            Assert.assertEquals("Duplicate aggregate creation.", commandResult.getResult());
            TestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
            Assert.assertNotNull(note);
            Assert.assertEquals("Note Title", note.getTitle());
            Assert.assertEquals(1, note.version());

            //执行创建聚合根的命令
            command = new CreateTestAggregateCommand();
            command.id = commandId;
            command.aggregateRootId = aggregateId;
            command.setTitle("Sample Note");
            asyncResult = Task.get(_commandService.executeAsync(command));
            Assert.assertNotNull(asyncResult);
            Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
            commandResult = asyncResult.getData();
            Assert.assertNotNull(commandResult);
            Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
            note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
            Assert.assertNotNull(note);
            Assert.assertEquals("Note Title", note.getTitle());
            Assert.assertEquals(1, note.version());
        } finally {
            _eventStore.setSupportBatchAppendEvent(true);
        }
    }

    @Test

    public void update_concurrent_conflict_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");

        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.version());

        TestAggregateTitleChanged aggregateTitleChanged = new TestAggregateTitleChanged("Changed Title");
        aggregateTitleChanged.setAggregateRootId(aggregateId);
        aggregateTitleChanged.setVersion(2);
        //往EventStore直接插入事件，用于模拟并发冲突的情况
        DomainEventStream eventStream = new DomainEventStream(
                ObjectId.generateNewStringId(),
                aggregateId,
                TestAggregate.class.getName(),
                2,
                new Date(),
                Lists.newArrayList(aggregateTitleChanged),
                null);
        AsyncTaskResult<EventAppendResult> result = Task.get(_eventStore.appendAsync(eventStream));
        Assert.assertNotNull(result);
        Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
        Assert.assertEquals(EventAppendResult.Success, result.getData());

        AsyncTaskResult result2 = Task.get(_publishedVersionStore.updatePublishedVersionAsync("DefaultEventProcessor", TestAggregate.class.getName(), aggregateId, 2));
        Assert.assertNotNull(result2);
        Assert.assertEquals(AsyncTaskStatus.Success, result2.getStatus());

        List<ICommand> commandList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ChangeTestAggregateTitleCommand command1 = new ChangeTestAggregateTitleCommand();
            command1.aggregateRootId = aggregateId;
            command1.setTitle("Changed Note2");
            commandList.add(command1);
        }


        ManualResetEvent waitHandle = new ManualResetEvent(false);
        long count = 0L;
        for (ICommand updateCommand : commandList) {
            _commandService.executeAsync(updateCommand).thenAccept(t ->
            {
                Assert.assertNotNull(t);
                Assert.assertEquals(AsyncTaskStatus.Success, t.getStatus());
                CommandResult currentCommandResult = t.getData();
                Assert.assertNotNull(currentCommandResult);
                Assert.assertEquals(CommandStatus.Success, currentCommandResult.getStatus());
                long totalCount = new AtomicLong(count).incrementAndGet();
                if (totalCount == commandList.size()) {
                    waitHandle.set();
                }
            });
        }
        waitHandle.waitOne();
        note = Task.get(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals(2 + commandList.size(), note.version());
        Assert.assertEquals("Changed Note2", note.getTitle());
    }

    @Test

    public void update_concurrent_conflict_not_enable_batch_insert_test() {
        _eventStore.setSupportBatchAppendEvent(false);

        try {
            update_concurrent_conflict_test();
        } finally {
            _eventStore.setSupportBatchAppendEvent(true);
        }
    }


    @Test

    public void event_handler_priority_test() {
        String noteId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command1 = new CreateTestAggregateCommand();
        command1.aggregateRootId = noteId;
        command1.setTitle("Sample Title1");
        TestEventPriorityCommand command2 = new TestEventPriorityCommand();
        command2.aggregateRootId = noteId;
        CommandResult commandResult1 = Task.get(_commandService.executeAsync(command1, CommandReturnType.EventHandled)).getData();
        CommandResult commandResult2 = Task.get(_commandService.executeAsync(command2, CommandReturnType.EventHandled)).getData();

        Task.sleep(3000);

        Assert.assertEquals(CommandStatus.Success, commandResult1.getStatus());
        Assert.assertEquals(CommandStatus.Success, commandResult2.getStatus());

        Assert.assertEquals(3, HandlerTypes.get(1).size());
        Assert.assertEquals(Handler3.class.getName(), HandlerTypes.get(1).get(0));
        Assert.assertEquals(Handler2.class.getName(), HandlerTypes.get(1).get(1));
        Assert.assertEquals(Handler1.class.getName(), HandlerTypes.get(1).get(2));

        Assert.assertEquals(3, HandlerTypes.get(2).size());
        Assert.assertEquals(Handler122.class.getName(), HandlerTypes.get(2).get(0));
        Assert.assertEquals(Handler121.class.getName(), HandlerTypes.get(2).get(1));
        Assert.assertEquals(Handler123.class.getName(), HandlerTypes.get(2).get(2));

        Assert.assertEquals(3, HandlerTypes.get(3).size());
        Assert.assertEquals(Handler1232.class.getName(), HandlerTypes.get(3).get(0));
        Assert.assertEquals(Handler1231.class.getName(), HandlerTypes.get(3).get(1));
        Assert.assertEquals(Handler1233.class.getName(), HandlerTypes.get(3).get(2));

        HandlerTypes.clear();
    }

    @Test

    public void sequence_domain_event_process_test() {
        TestAggregate note = new TestAggregate(ObjectId.generateNewStringId(), "initial title");
        IAggregateRoot aggregate = note;
        DomainEventStreamMessage message1 = CreateMessage(aggregate);

        aggregate.acceptChanges(1);
        note.ChangeTitle("title1");
        DomainEventStreamMessage message2 = CreateMessage(aggregate);

        aggregate.acceptChanges(2);
        note.ChangeTitle("title2");
        DomainEventStreamMessage message3 = CreateMessage(aggregate);

        ManualResetEvent waitHandle = new ManualResetEvent(false);
        List<Integer> versionList = new ArrayList<>();
        processor.process(new ProcessingDomainEventStreamMessage(message1, new DomainEventStreamProcessContext(message1, waitHandle, versionList)));
        processor.process(new ProcessingDomainEventStreamMessage(message3, new DomainEventStreamProcessContext(message3, waitHandle, versionList)));
        processor.process(new ProcessingDomainEventStreamMessage(message2, new DomainEventStreamProcessContext(message2, waitHandle, versionList)));

        waitHandle.waitOne();

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(i + 1, versionList.get(i).intValue());
        }
    }

    private DomainEventStreamMessage CreateMessage(IAggregateRoot aggregateRoot) {
        return new DomainEventStreamMessage(
                ObjectId.generateNewStringId(),
                aggregateRoot.uniqueId(),
                aggregateRoot.version() + 1,
                aggregateRoot.getClass().getName(),
                aggregateRoot.getChanges(),
                Maps.newHashMap()
        );
    }
}
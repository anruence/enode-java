package org.enodeframework.tests.TestClasses;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.threading.ManualResetEvent;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.EventAppendResult;
import org.enodeframework.eventing.ProcessingEvent;
import org.enodeframework.tests.Commands.AggregateThrowExceptionCommand;
import org.enodeframework.tests.Commands.AsyncHandlerBaseCommand;
import org.enodeframework.tests.Commands.AsyncHandlerChildCommand;
import org.enodeframework.tests.Commands.AsyncHandlerCommand;
import org.enodeframework.tests.Commands.BaseCommand;
import org.enodeframework.tests.Commands.ChangeInheritTestAggregateTitleCommand;
import org.enodeframework.tests.Commands.ChangeMultipleAggregatesCommand;
import org.enodeframework.tests.Commands.ChangeNothingCommand;
import org.enodeframework.tests.Commands.ChangeTestAggregateTitleCommand;
import org.enodeframework.tests.Commands.ChildCommand;
import org.enodeframework.tests.Commands.CreateInheritTestAggregateCommand;
import org.enodeframework.tests.Commands.CreateTestAggregateCommand;
import org.enodeframework.tests.Commands.NoHandlerCommand;
import org.enodeframework.tests.Commands.NotCheckAsyncHandlerExistWithResultCommand;
import org.enodeframework.tests.Commands.SetResultCommand;
import org.enodeframework.tests.Commands.TestEventPriorityCommand;
import org.enodeframework.tests.Commands.ThrowExceptionCommand;
import org.enodeframework.tests.Commands.TwoAsyncHandlersCommand;
import org.enodeframework.tests.Commands.TwoHandlersCommand;
import org.enodeframework.tests.Domain.InheritTestAggregate;
import org.enodeframework.tests.Domain.TestAggregate;
import org.enodeframework.tests.Domain.TestAggregateCreated;
import org.enodeframework.tests.Domain.TestAggregateTitleChanged;
import org.enodeframework.tests.EventHandlers.Handler1;
import org.enodeframework.tests.EventHandlers.Handler121;
import org.enodeframework.tests.EventHandlers.Handler122;
import org.enodeframework.tests.EventHandlers.Handler123;
import org.enodeframework.tests.EventHandlers.Handler1231;
import org.enodeframework.tests.EventHandlers.Handler1232;
import org.enodeframework.tests.EventHandlers.Handler1233;
import org.enodeframework.tests.EventHandlers.Handler2;
import org.enodeframework.tests.EventHandlers.Handler3;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CommandAndEventServiceTest extends AbstractTest {
    public static ConcurrentHashMap<Integer, List<String>> HandlerTypes = new ConcurrentHashMap<>();

    private static Logger _logger = LoggerFactory.getLogger(CommandAndEventServiceTest.class);

    @Test
    public void create_and_update_aggregate_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
        //执行修改聚合根的命令
        ChangeTestAggregateTitleCommand command2 = new ChangeTestAggregateTitleCommand();
        command2.aggregateRootId = aggregateId;
        command2.setTitle("Changed Note");
        asyncResult = Task.await(_commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.getVersion());
    }

    @Test
    public void create_and_update_inherit_aggregate_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateInheritTestAggregateCommand command = new CreateInheritTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        InheritTestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, InheritTestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
        //执行修改聚合根的命令
        ChangeInheritTestAggregateTitleCommand command2 = new ChangeInheritTestAggregateTitleCommand();
        command2.aggregateRootId = aggregateId;
        command2.setTitle("Changed Note");
        asyncResult = Task.await(_commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        note = Task.await(_memoryCache.getAsync(aggregateId, InheritTestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.getVersion());
    }

    @Test
    public void command_sync_execute_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        command.setSleepMilliseconds(3000);
        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> commandResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getData().getStatus());
        TestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
        //执行修改聚合根的命令
        ChangeTestAggregateTitleCommand command2 = new ChangeTestAggregateTitleCommand();
        command2.aggregateRootId = aggregateId;
        command2.setTitle("Changed Note");
        commandResult = Task.await(_commandService.executeAsync(command2));
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getData().getStatus());
        note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.getVersion());
    }

    @Test
    public void duplicate_create_aggregate_command_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
        //用同一个命令再次执行创建聚合根的命令
        asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
        //用另一个命令再次执行创建相同聚合根的命令
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
    }

    @Test
    public void duplicate_update_aggregate_command_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command1 = new CreateTestAggregateCommand();
        command1.aggregateRootId = aggregateId;
        command1.setTitle("Sample Note");
        //先创建一个聚合根
        CommandStatus status = Task.await(_commandService.executeAsync(command1)).getData().getStatus();
        Assert.assertEquals(CommandStatus.Success, status);
        ChangeTestAggregateTitleCommand command2 = new ChangeTestAggregateTitleCommand();
        command2.aggregateRootId = aggregateId;
        command2.setTitle("Changed Note");
        //执行修改聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command2, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.getVersion());
        //在重复执行该命令
        asyncResult = Task.await(_commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Changed Note", note.getTitle());
        Assert.assertEquals(2, note.getVersion());
    }

    @Test
    public void create_and_concurrent_update_aggregate_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
        //并发执行修改聚合根的命令
        long totalCount = 100;
        AtomicLong finishedCount = new AtomicLong(0);
        ManualResetEvent waitHandle = new ManualResetEvent(false);
        for (int i = 0; i < totalCount; i++) {
            ChangeTestAggregateTitleCommand updateCommand = new ChangeTestAggregateTitleCommand();
            updateCommand.aggregateRootId = aggregateId;
            updateCommand.setTitle("Changed Note");
            _commandService.executeAsync(updateCommand).thenAccept(result ->
            {
                Assert.assertNotNull(result);
                Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
                Assert.assertNotNull(result.getData());
                Assert.assertEquals(CommandStatus.Success, result.getData().getStatus());
                long current = finishedCount.incrementAndGet();
                if (current == totalCount) {
                    TestAggregate note1 = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
                    Assert.assertNotNull(note1);
                    Assert.assertEquals("Changed Note", note1.getTitle());
                    Assert.assertEquals(totalCount + 1, ((IAggregateRoot) note1).getVersion());
                    waitHandle.set();
                }
            });
        }
        waitHandle.waitOne();
    }

    @Test
    public void perf_create_and_concurrent_update_aggregate_test() throws InterruptedException {
        //并发执行创建聚合根的命令
        int totalCount = 100;
        AtomicLong finishedCount = new AtomicLong(0);
        ManualResetEvent waitHandle = new ManualResetEvent(false);
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(totalCount);
        for (int i = 0; i < totalCount; i++) {
            CreateTestAggregateCommand command = new CreateTestAggregateCommand();
            command.aggregateRootId = ObjectId.generateNewStringId();
            command.setTitle("Sample Note" + ObjectId.generateNewStringId());
            try {
                _commandService.executeAsync(command).thenAccept(result -> {
                    _logger.info("{}", result);
                    latch.countDown();
                });
            } catch (Exception e) {
                latch.countDown();
            }
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("run tin " + (end - start));
    }

    @Test
    public void change_nothing_test() {
        ChangeNothingCommand command = new ChangeNothingCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command, CommandReturnType.EventHandled));
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
        command1.aggregateRootId = ObjectId.generateNewStringId();
        command1.setTitle("Sample Note1");
        Task.await(_commandService.executeAsync(command1));
        CreateTestAggregateCommand command2 = new CreateTestAggregateCommand();
        command2.aggregateRootId = ObjectId.generateNewStringId();
        command2.setTitle("Sample Note2");
        Task.await(_commandService.executeAsync(command2));
        ChangeMultipleAggregatesCommand command3 = new ChangeMultipleAggregatesCommand();
        command3.aggregateRootId = ObjectId.generateNewStringId();
        command3.setAggregateRootId1(command1.getAggregateRootId());
        command3.setAggregateRootId2(command2.getAggregateRootId());
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command3));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
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
        Task.await(_commandService.executeAsync(command));
        AggregateThrowExceptionCommand command1 = new AggregateThrowExceptionCommand();
        command1.aggregateRootId = aggregateId;
        command1.setPublishableException(false);
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        AggregateThrowExceptionCommand command2 = new AggregateThrowExceptionCommand();
        command2.aggregateRootId = aggregateId;
        command2.setPublishableException(true);
        asyncResult = Task.await(_commandService.executeAsync(command2));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.NothingChanged, commandResult.getStatus());
        Assert.assertEquals("ResultFromBaseCommand", commandResult.getResult());
        command = new ChildCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        AsyncHandlerCommand command1 = new AsyncHandlerCommand();
        command1.aggregateRootId = ObjectId.generateNewStringId();
        command1.setShouldThrowIOException(true);
        asyncResult = Task.await(_commandService.executeAsync(command1));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        command = new AsyncHandlerChildCommand();
        command.aggregateRootId = (ObjectId.generateNewStringId());
        asyncResult = Task.await(_commandService.executeAsync(command));
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
        AsyncTaskResult<EventAppendResult> result = Task.await(_eventStore.batchAppendAsync(Lists.newArrayList(eventStream)));
        Assert.assertNotNull(result);
        Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
        assertAppendResult(result.getData());
        _logger.info("----create_concurrent_conflict_and_then_update_many_times_test, _eventStore.appendAsync success");
        AsyncTaskResult result2 = Task.await(_publishedVersionStore.updatePublishedVersionAsync("DefaultEventProcessor", TestAggregate.class.getName(), aggregateId, 1));
        Assert.assertNotNull(result2);
        Assert.assertEquals(AsyncTaskStatus.Success, result2.getStatus());
        _logger.info("----create_concurrent_conflict_and_then_update_many_times_test, UpdatePublishedversion()Async success");
        //执行创建聚合根的命令
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.setId(commandId);
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
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
        AtomicLong count = new AtomicLong(0);
        for (ICommand updateCommand : commandList) {
            _commandService.executeAsync(updateCommand).thenAccept(t ->
            {
                Assert.assertNotNull(t);
                Assert.assertEquals(AsyncTaskStatus.Success, t.getStatus());
                CommandResult updateCommandResult = t.getData();
                Assert.assertNotNull(updateCommandResult);
                Assert.assertEquals(CommandStatus.Success, updateCommandResult.getStatus());
                long totalCount = count.incrementAndGet();
                _logger.info("----create_concurrent_conflict_and_then_update_many_times_test, updateCommand finished, count: {}", totalCount);
                if (totalCount == commandList.size()) {
                    waitHandle.set();
                }
            });
        }
        waitHandle.waitOne();
        TestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals(commandList.size() + 1, note.getVersion());
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
                Maps.newHashMap());
        AsyncTaskResult<EventAppendResult> result = Task.await(_eventStore.batchAppendAsync(Lists.newArrayList(eventStream)));
        Assert.assertNotNull(result);
        Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
        assertAppendResult(result.getData());
        AsyncTaskResult result2 = Task.await(_publishedVersionStore.updatePublishedVersionAsync("DefaultEventProcessor", TestAggregate.class.getName(), aggregateId, 1));
        Assert.assertNotNull(result2);
        Assert.assertEquals(AsyncTaskStatus.Success, result2.getStatus());
        List<ICommand> commandList = new ArrayList<>();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.setId(commandId);
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
        AtomicLong count = new AtomicLong(0);
        AtomicBoolean createCommandSuccess = new AtomicBoolean(false);
        for (ICommand updateCommand : commandList) {
            _commandService.executeAsync(updateCommand).thenAccept(t ->
            {
                Assert.assertNotNull(t);
                Assert.assertEquals(AsyncTaskStatus.Success, t.getStatus());
                CommandResult commandResult = t.getData();
                Assert.assertNotNull(commandResult);
                Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
                if (!Objects.equals(commandResult.getCommandId(), commandId)) {
                    long totalCount = count.incrementAndGet();
                    if (totalCount == commandList.size() - 1) {
                        waitHandle.set();
                    }
                } else {
                    createCommandSuccess.set(true);
                }
            });
        }
        waitHandle.waitOne();
        TestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertTrue(createCommandSuccess.get());
        Assert.assertEquals(commandList.size(), note.getVersion());
    }

    @Test
    public void create_concurrent_conflict_not_enable_batch_insert_test() {
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
        AsyncTaskResult<EventAppendResult> result = Task.await(_eventStore.batchAppendAsync(Lists.newArrayList(eventStream)));
        Assert.assertNotNull(result);
        Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
        assertAppendResult(result.getData());
        AsyncTaskResult result2 = Task.await(_publishedVersionStore.updatePublishedVersionAsync("DefaultEventProcessor", TestAggregate.class.getName(), aggregateId, 1));
        Assert.assertNotNull(result2);
        Assert.assertEquals(AsyncTaskStatus.Success, result2.getStatus());
        //执行创建聚合根的命令
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        Assert.assertEquals("Duplicate aggregate creation.", commandResult.getResult());
        TestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Note Title", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
        //执行创建聚合根的命令
        command = new CreateTestAggregateCommand();
        command.setId(commandId);
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Note Title", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
    }

    @Test
    public void update_concurrent_conflict_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        //执行创建聚合根的命令
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        TestAggregate note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals("Sample Note", note.getTitle());
        Assert.assertEquals(1, note.getVersion());
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
        AsyncTaskResult<EventAppendResult> result = Task.await(_eventStore.batchAppendAsync(Lists.newArrayList(eventStream)));
        Assert.assertNotNull(result);
        Assert.assertEquals(AsyncTaskStatus.Success, result.getStatus());
        assertAppendResult(result.getData());
        AsyncTaskResult result2 = Task.await(_publishedVersionStore.updatePublishedVersionAsync("DefaultEventProcessor", TestAggregate.class.getName(), aggregateId, 2));
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
        AtomicLong count = new AtomicLong(0);
        for (ICommand updateCommand : commandList) {
            _commandService.executeAsync(updateCommand).thenAccept(t ->
            {
                Assert.assertNotNull(t);
                Assert.assertEquals(AsyncTaskStatus.Success, t.getStatus());
                CommandResult currentCommandResult = t.getData();
                Assert.assertNotNull(currentCommandResult);
                Assert.assertEquals(CommandStatus.Success, currentCommandResult.getStatus());
                long totalCount = count.incrementAndGet();
                if (totalCount == commandList.size()) {
                    waitHandle.set();
                }
            });
        }
        waitHandle.waitOne();
        note = Task.await(_memoryCache.getAsync(aggregateId, TestAggregate.class));
        Assert.assertNotNull(note);
        Assert.assertEquals(2 + commandList.size(), note.getVersion());
        Assert.assertEquals("Changed Note2", note.getTitle());
    }


    @Test
    public void event_handler_priority_test() {
        String noteId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command1 = new CreateTestAggregateCommand();
        command1.aggregateRootId = noteId;
        command1.setTitle("Sample Title1");
        TestEventPriorityCommand command2 = new TestEventPriorityCommand();
        command2.aggregateRootId = noteId;
        CommandResult commandResult1 = Task.await(_commandService.executeAsync(command1, CommandReturnType.EventHandled)).getData();
        CommandResult commandResult2 = Task.await(_commandService.executeAsync(command2, CommandReturnType.EventHandled)).getData();
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
        DomainEventStreamMessage message1 = createMessage(note);
        ((IAggregateRoot) note).acceptChanges();
        note.ChangeTitle("title1");
        DomainEventStreamMessage message2 = createMessage(note);
        ((IAggregateRoot) note).acceptChanges();
        note.ChangeTitle("title2");
        DomainEventStreamMessage message3 = createMessage(note);
        ManualResetEvent waitHandle = new ManualResetEvent(false);
        List<Integer> versionList = new ArrayList<>();
        processor.process(new ProcessingEvent(message1, new DomainEventStreamProcessContext(message1, waitHandle, versionList)));
        processor.process(new ProcessingEvent(message3, new DomainEventStreamProcessContext(message3, waitHandle, versionList)));
        processor.process(new ProcessingEvent(message2, new DomainEventStreamProcessContext(message2, waitHandle, versionList)));
        waitHandle.waitOne();
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(i + 1, versionList.get(i).intValue());
        }
    }

    private DomainEventStreamMessage createMessage(IAggregateRoot aggregateRoot) {
        return new DomainEventStreamMessage(
                ObjectId.generateNewStringId(),
                aggregateRoot.getUniqueId(),
                aggregateRoot.getVersion() + 1,
                aggregateRoot.getClass().getName(),
                aggregateRoot.getChanges(),
                Maps.newHashMap()
        );
    }

    private void assertAppendResult(EventAppendResult appendResult) {
        Assert.assertEquals(1, appendResult.getSuccessAggregateRootIdList().size());
        Assert.assertEquals(0, appendResult.getDuplicateCommandIdList().size());
        Assert.assertEquals(0, appendResult.getDuplicateEventAggregateRootIdList().size());
    }
}
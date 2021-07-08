package org.enodeframework.test;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utils.IdGenerator;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.test.command.AggregateThrowExceptionCommand;
import org.enodeframework.test.command.AsyncHandlerCommand;
import org.enodeframework.test.command.CreateTestAggregateCommand;
import org.enodeframework.test.config.TestMockConfig;
import org.enodeframework.test.mock.FailedType;
import org.enodeframework.test.mock.MockApplicationMessagePublisher;
import org.enodeframework.test.mock.MockDomainEventPublisher;
import org.enodeframework.test.mock.MockEventStore;
import org.enodeframework.test.mock.MockPublishableExceptionPublisher;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = MessagePublisherTest.MyPropertyInitializer.class, classes = {TestMockConfig.class})
public class MessagePublisherTest extends AbstractTest {

    @Autowired
    @Qualifier("mockDomainEventPublisher")
    protected IMessagePublisher<DomainEventStreamMessage> domainEventPublisher;

    @Autowired
    @Qualifier("mockApplicationMessagePublisher")
    protected IMessagePublisher<IApplicationMessage> applicationMessagePublisher;

    @Autowired
    @Qualifier("mockPublishableExceptionPublisher")
    protected IMessagePublisher<IDomainException> publishableExceptionPublisher;

    @Test
    public void event_store_failed_test() {
        MockEventStore mockEventStore = (MockEventStore) eventStore;
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setTitle("Sample Note");
        mockEventStore.SetExpectFailedCount(FailedType.UnKnownException, 5);
        CommandResult asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        CommandResult commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockEventStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setTitle("Sample Note");
        mockEventStore.SetExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockEventStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setTitle("Sample Note");
        mockEventStore.SetExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockEventStore.Reset();
    }

    @Test
    public void publishable_exception_publisher_throw_exception_test() {
        String aggregateId = IdGenerator.nextId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        Task.await(commandService.executeAsync(command));
        AggregateThrowExceptionCommand command1 = new AggregateThrowExceptionCommand();
        command1.aggregateRootId = aggregateId;
        command1.setPublishableException(true);
        ((MockPublishableExceptionPublisher) publishableExceptionPublisher).SetExpectFailedCount(FailedType.UnKnownException, 5);
        CommandResult asyncResult = Task.await(commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);

        CommandResult commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        ((MockPublishableExceptionPublisher) publishableExceptionPublisher).Reset();
        ((MockPublishableExceptionPublisher) publishableExceptionPublisher).SetExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        ((MockPublishableExceptionPublisher) publishableExceptionPublisher).Reset();
        ((MockPublishableExceptionPublisher) publishableExceptionPublisher).SetExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        ((MockPublishableExceptionPublisher) publishableExceptionPublisher).Reset();
    }

    @Test
    public void published_version_store_failed_test() {
        MockEventStore mockPublishedVersionStore = (MockEventStore) eventStore;
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setTitle("Sample Note");
        mockPublishedVersionStore.SetExpectFailedCount(FailedType.UnKnownException, 5);
        CommandResult asyncResult = Task.await(commandService.executeAsync(command, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);

        CommandResult commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockPublishedVersionStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setTitle("Sample Note");
        mockPublishedVersionStore.SetExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockPublishedVersionStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setTitle("Sample Note");
        mockPublishedVersionStore.SetExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockPublishedVersionStore.Reset();
    }

    @Test

    public void event_publisher_failed_test() {
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setTitle("Sample Note");
        ((MockDomainEventPublisher) domainEventPublisher).setExpectFailedCount(FailedType.UnKnownException, 5);
        CommandResult asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        CommandResult commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockDomainEventPublisher) domainEventPublisher).Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setTitle("Sample Note");
        ((MockDomainEventPublisher) domainEventPublisher).setExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockDomainEventPublisher) domainEventPublisher).Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setTitle("Sample Note");
        ((MockDomainEventPublisher) domainEventPublisher).setExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockDomainEventPublisher) domainEventPublisher).Reset();
    }

    @Test
    public void async_command_application_message_publish_failed_test() {
        MockApplicationMessagePublisher mockApplicationMessagePublisher = (MockApplicationMessagePublisher) applicationMessagePublisher;
        mockApplicationMessagePublisher.SetExpectFailedCount(FailedType.UnKnownException, 5);
        AsyncHandlerCommand command = new AsyncHandlerCommand();
        command.aggregateRootId = IdGenerator.nextId();
        command.setShouldGenerateApplicationMessage(true);
        CommandResult asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        CommandResult commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockApplicationMessagePublisher.Reset();
        mockApplicationMessagePublisher.SetExpectFailedCount(FailedType.IOException, 5);
        AsyncHandlerCommand command1 = new AsyncHandlerCommand();
        command1.aggregateRootId = IdGenerator.nextId();
        command1.setShouldGenerateApplicationMessage(true);
        asyncResult = Task.await(commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockApplicationMessagePublisher.Reset();
        mockApplicationMessagePublisher.SetExpectFailedCount(FailedType.TaskIOException, 5);
        AsyncHandlerCommand command2 = new AsyncHandlerCommand();
        command2.aggregateRootId = IdGenerator.nextId();
        command2.setShouldGenerateApplicationMessage(true);
        asyncResult = Task.await(commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockApplicationMessagePublisher.Reset();
    }

    static class MyPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.enode.eventstore=mock").applyTo(applicationContext);
        }

    }
}

package com.enodeframework.tests.TestClasses;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandStatus;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Task;
import com.enodeframework.common.utilities.ObjectId;
import com.enodeframework.tests.Commands.CreateTestAggregateCommand;
import com.enodeframework.tests.Mocks.FailedType;
import com.enodeframework.tests.Mocks.MockDomainEventPublisher;
import org.junit.Assert;
import org.junit.Test;

public class EventPublisherFailedTest extends AbstractTest {
    @Test
    public void event_publisher_failed_test() {
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        ((MockDomainEventPublisher) _domainEventPublisher).setExpectFailedCount(FailedType.UnKnownException, 5);
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockDomainEventPublisher) _domainEventPublisher).Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        ((MockDomainEventPublisher) _domainEventPublisher).setExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockDomainEventPublisher) _domainEventPublisher).Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        ((MockDomainEventPublisher) _domainEventPublisher).setExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockDomainEventPublisher) _domainEventPublisher).Reset();
    }
}

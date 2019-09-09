package org.enodeframework.tests.TestClasses;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.tests.Commands.CreateTestAggregateCommand;
import org.enodeframework.tests.Mocks.FailedType;
import org.enodeframework.tests.Mocks.MockDomainEventPublisher;
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

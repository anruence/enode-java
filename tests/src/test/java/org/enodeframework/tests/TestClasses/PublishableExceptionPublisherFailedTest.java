package org.enodeframework.tests.TestClasses;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.tests.Commands.AggregateThrowExceptionCommand;
import org.enodeframework.tests.Commands.CreateTestAggregateCommand;
import org.enodeframework.tests.Mocks.FailedType;
import org.enodeframework.tests.Mocks.MockPublishableExceptionPublisher;
import org.junit.Assert;
import org.junit.Test;

public class PublishableExceptionPublisherFailedTest extends AbstractTest {
    @Test
    public void publishable_exception_publisher_throw_exception_test() {
        String aggregateId = ObjectId.generateNewStringId();
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = aggregateId;
        command.setTitle("Sample Note");
        Task.await(_commandService.executeAsync(command));
        AggregateThrowExceptionCommand command1 = new AggregateThrowExceptionCommand();
        command1.aggregateRootId = aggregateId;
        command1.setPublishableException(true);
        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).SetExpectFailedCount(FailedType.UnKnownException, 5);
        CommandResult asyncResult = Task.await(_commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);

        CommandResult commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).Reset();
        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).SetExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.await(_commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).Reset();
        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).SetExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.await(_commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).Reset();
    }
}

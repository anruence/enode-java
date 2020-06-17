package org.enodeframework.tests.testclasses;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.tests.commands.AggregateThrowExceptionCommand;
import org.enodeframework.tests.commands.CreateTestAggregateCommand;
import org.enodeframework.tests.mocks.FailedType;
import org.enodeframework.tests.mocks.MockPublishableExceptionPublisher;
import org.junit.Assert;
import org.junit.Test;

public class PublishableExceptionPublisherFailedTest extends AbstractTest {
    public void publishable_exception_publisher_throw_exception_test() {
        String aggregateId = ObjectId.generateNewStringId();
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
    public void test() {

    }
}

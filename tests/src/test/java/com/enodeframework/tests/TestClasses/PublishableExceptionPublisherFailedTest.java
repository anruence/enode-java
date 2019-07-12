package com.enodeframework.tests.TestClasses;

import com.enodeframework.tests.Commands.AggregateThrowExceptionCommand;
import com.enodeframework.tests.Commands.CreateTestAggregateCommand;
import com.enodeframework.tests.Mocks.FailedType;
import com.enodeframework.tests.Mocks.MockPublishableExceptionPublisher;
import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandStatus;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Task;
import com.enodeframework.common.utilities.ObjectId;
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

        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command1));

        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).Reset();

        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).SetExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.get(_commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).Reset();

        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).SetExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.get(_commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Failed, commandResult.getStatus());
        ((MockPublishableExceptionPublisher) _publishableExceptionPublisher).Reset();
    }
}

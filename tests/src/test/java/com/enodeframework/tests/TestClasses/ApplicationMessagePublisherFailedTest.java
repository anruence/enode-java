package com.enodeframework.tests.TestClasses;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandStatus;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Task;
import com.enodeframework.common.utilities.ObjectId;
import com.enodeframework.tests.Commands.AsyncHandlerCommand;
import com.enodeframework.tests.Mocks.FailedType;
import com.enodeframework.tests.Mocks.MockApplicationMessagePublisher;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationMessagePublisherFailedTest extends AbstractTest {

    @Test
    public void async_command_application_message_publish_failed_test() {
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).SetExpectFailedCount(FailedType.UnKnownException, 5);
        AsyncHandlerCommand command = new AsyncHandlerCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setShouldGenerateApplicationMessage(true);
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).Reset();

        ((MockApplicationMessagePublisher) _applicationMessagePublisher).SetExpectFailedCount(FailedType.IOException, 5);

        AsyncHandlerCommand command1 = new AsyncHandlerCommand();
        command1.aggregateRootId = ObjectId.generateNewStringId();
        command1.setShouldGenerateApplicationMessage(true);
        asyncResult = Task.get(_commandService.executeAsync(command1));

        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).Reset();

        ((MockApplicationMessagePublisher) _applicationMessagePublisher).SetExpectFailedCount(FailedType.TaskIOException, 5);
        AsyncHandlerCommand command2 = new AsyncHandlerCommand();
        command2.aggregateRootId = ObjectId.generateNewStringId();
        command2.setShouldGenerateApplicationMessage(true);
        asyncResult = Task.get(_commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).Reset();
    }
}

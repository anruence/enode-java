package org.enodeframework.tests.TestClasses;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.tests.Commands.AsyncHandlerCommand;
import org.enodeframework.tests.Mocks.FailedType;
import org.enodeframework.tests.Mocks.MockApplicationMessagePublisher;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationMessagePublisherFailedTest extends AbstractTest {
    @Test
    public void async_command_application_message_publish_failed_test() {
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).SetExpectFailedCount(FailedType.UnKnownException, 5);
        AsyncHandlerCommand command = new AsyncHandlerCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setShouldGenerateApplicationMessage(true);
        CommandResult asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        CommandResult commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).Reset();
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).SetExpectFailedCount(FailedType.IOException, 5);
        AsyncHandlerCommand command1 = new AsyncHandlerCommand();
        command1.aggregateRootId = ObjectId.generateNewStringId();
        command1.setShouldGenerateApplicationMessage(true);
        asyncResult = Task.await(_commandService.executeAsync(command1));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).Reset();
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).SetExpectFailedCount(FailedType.TaskIOException, 5);
        AsyncHandlerCommand command2 = new AsyncHandlerCommand();
        command2.aggregateRootId = ObjectId.generateNewStringId();
        command2.setShouldGenerateApplicationMessage(true);
        asyncResult = Task.await(_commandService.executeAsync(command2));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        ((MockApplicationMessagePublisher) _applicationMessagePublisher).Reset();
    }
}

package com.enodeframework.tests.TestClasses;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.commanding.CommandStatus;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Task;
import com.enodeframework.common.utilities.ObjectId;
import com.enodeframework.tests.Commands.CreateTestAggregateCommand;
import com.enodeframework.tests.Mocks.FailedType;
import com.enodeframework.tests.Mocks.MockPublishedVersionStore;
import org.junit.Assert;
import org.junit.Test;

public class PublishedVersionStoreFailedTest extends AbstractTest {

    @Test
    public void published_version_store_failed_test() {
        MockPublishedVersionStore mockPublishedVersionStore = (MockPublishedVersionStore) _publishedVersionStore;
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");

        mockPublishedVersionStore.SetExpectFailedCount(FailedType.UnKnownException, 5);
        AsyncTaskResult<CommandResult> asyncResult = Task.get(_commandService.executeAsync(command, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockPublishedVersionStore.Reset();

        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");

        mockPublishedVersionStore.SetExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.get(_commandService.executeAsync(command, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockPublishedVersionStore.Reset();

        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");

        mockPublishedVersionStore.SetExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.get(_commandService.executeAsync(command, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockPublishedVersionStore.Reset();
    }
}

package org.enodeframework.tests.testclasses;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.tests.commands.CreateTestAggregateCommand;
import org.enodeframework.tests.mocks.FailedType;
import org.enodeframework.tests.mocks.MockPublishedVersionStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PublishedVersionStoreFailedTest extends AbstractTest {

    @Before
    public void initPublishedVersionStore() {
        publishedVersionStore = new MockPublishedVersionStore();
    }

    @Test
    public void published_version_store_failed_test() {
        MockPublishedVersionStore mockPublishedVersionStore = (MockPublishedVersionStore) publishedVersionStore;
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        mockPublishedVersionStore.SetExpectFailedCount(FailedType.UnKnownException, 5);
        CommandResult asyncResult = Task.await(commandService.executeAsync(command, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);

        CommandResult commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockPublishedVersionStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        mockPublishedVersionStore.SetExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command, CommandReturnType.EventHandled));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockPublishedVersionStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
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
    public void test() {

    }
}

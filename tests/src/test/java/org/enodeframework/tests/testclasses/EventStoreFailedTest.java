package org.enodeframework.tests.testclasses;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.tests.commands.CreateTestAggregateCommand;
import org.enodeframework.tests.mocks.FailedType;
import org.enodeframework.tests.mocks.MockEventStore;
import org.junit.Assert;
import org.junit.Test;

public class EventStoreFailedTest extends AbstractTest {

    public void event_store_failed_test() {
        MockEventStore mockEventStore = (MockEventStore) eventStore;
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        mockEventStore.SetExpectFailedCount(FailedType.UnKnownException, 5);
        CommandResult asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        CommandResult commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockEventStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        mockEventStore.SetExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockEventStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        mockEventStore.SetExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.await(commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);

        commandResult = asyncResult;
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockEventStore.Reset();
    }

    public void esfindAsync() {
        String aId = "5d3acc9dd1fcfe66c9b0b324";
        eventStore.findAsync(aId, 1).thenAccept(x -> {
        });
    }

    public void esfindAsyncWithCommand() {
        String aId = "5d3acc9dd1fcfe66c9b0b324";
        String cid = "5d3acc9ed1fcfe66c9b0b346";
        eventStore.findAsync(aId, cid).thenAccept(x -> {
        });
    }

    @Test
    public void test() {

    }
}

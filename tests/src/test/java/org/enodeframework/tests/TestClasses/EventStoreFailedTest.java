package org.enodeframework.tests.TestClasses;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.tests.Commands.CreateTestAggregateCommand;
import org.enodeframework.tests.Mocks.FailedType;
import org.enodeframework.tests.Mocks.MockEventStore;
import org.junit.Assert;
import org.junit.Test;

public class EventStoreFailedTest extends AbstractTest {

    @Test
    public void event_store_failed_test() {
        MockEventStore mockEventStore = (MockEventStore) _eventStore;
        CreateTestAggregateCommand command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        mockEventStore.SetExpectFailedCount(FailedType.UnKnownException, 5);
        AsyncTaskResult<CommandResult> asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        CommandResult commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockEventStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        mockEventStore.SetExpectFailedCount(FailedType.IOException, 5);
        asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockEventStore.Reset();
        command = new CreateTestAggregateCommand();
        command.aggregateRootId = ObjectId.generateNewStringId();
        command.setTitle("Sample Note");
        mockEventStore.SetExpectFailedCount(FailedType.TaskIOException, 5);
        asyncResult = Task.await(_commandService.executeAsync(command));
        Assert.assertNotNull(asyncResult);
        Assert.assertEquals(AsyncTaskStatus.Success, asyncResult.getStatus());
        commandResult = asyncResult.getData();
        Assert.assertNotNull(commandResult);
        Assert.assertEquals(CommandStatus.Success, commandResult.getStatus());
        mockEventStore.Reset();
    }

    @Test
    public void esfindAsync() {
        String aId = "5d3acc9dd1fcfe66c9b0b324";
        try {
            _eventStore.findAsync(aId, 1).thenAccept(x -> {
                x.getData();
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void esfindAsyncWithCommand() {
        String aId = "5d3acc9dd1fcfe66c9b0b324";
        String cid = "5d3acc9ed1fcfe66c9b0b346";
        try {
            _eventStore.findAsync(aId, cid).thenAccept(x -> {
                x.getData();
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

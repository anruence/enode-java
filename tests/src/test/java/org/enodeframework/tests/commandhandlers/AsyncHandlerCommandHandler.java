package org.enodeframework.tests.commandhandlers;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.tests.commands.AsyncHandlerCommand;
import org.enodeframework.tests.commands.TwoAsyncHandlersCommand;
import org.springframework.beans.factory.annotation.Autowired;

@Command
public class AsyncHandlerCommandHandler {

    public boolean CheckCommandHandledFirst;
    @Autowired
    TestComponent testComponent;
    private int _count;

    @Subscribe
    public void handleAsync(ICommandContext context, AsyncHandlerCommand command) throws Exception {
        testComponent.sayHello();
        if (command.ShouldGenerateApplicationMessage) {
            context.setApplicationMessage(new TestApplicationMessage(command.getAggregateRootId()));
        } else if (command.ShouldThrowException) {
            throw new Exception("AsyncCommandException");
        } else if (command.ShouldThrowIOException) {
            _count++;
            if (_count <= 5) {
                throw new IORuntimeException("AsyncCommandIOException" + _count);
            }
            _count = 0;
        } else {
        }
    }

    @Subscribe
    public void HandleAsync1(TwoAsyncHandlersCommand command) {
    }

    @Subscribe
    public void HandleAsync2(TwoAsyncHandlersCommand command) {
    }
}
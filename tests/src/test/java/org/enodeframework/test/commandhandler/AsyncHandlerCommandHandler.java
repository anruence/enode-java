package org.enodeframework.test.commandhandler;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.test.command.AsyncHandlerCommand;
import org.enodeframework.test.command.TwoAsyncHandlersCommand;
import org.springframework.beans.factory.annotation.Autowired;

@Command
public class AsyncHandlerCommandHandler {

    @Autowired
    private TestComponent testComponent;

    private int count;

    @Subscribe
    public void handleAsync(ICommandContext context, AsyncHandlerCommand command) throws Exception {
        testComponent.sayHello();
        if (command.shouldGenerateApplicationMessage) {
            context.setApplicationMessage(new TestApplicationMessage(command.getAggregateRootId()));
        } else if (command.shouldThrowException) {
            throw new Exception("AsyncCommandException");
        } else if (command.shouldThrowIOException) {
            count++;
            if (count <= 5) {
                throw new IORuntimeException("AsyncCommandIOException" + count);
            }
            count = 0;
        }
    }

    @Subscribe
    public void handleAsync1(TwoAsyncHandlersCommand command) {
    }

    @Subscribe
    public void handleAsync2(TwoAsyncHandlersCommand command) {
    }
}
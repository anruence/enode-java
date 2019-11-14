package org.enodeframework.tests.CommandHandlers;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.tests.Commands.AsyncHandlerCommand;
import org.enodeframework.tests.Commands.TwoAsyncHandlersCommand;

@Command
public class AsyncHandlerCommandHandler {
    public boolean CheckCommandHandledFirst;
    private int _count;

    @Subscribe
    public void handleAsync(ICommandContext context, AsyncHandlerCommand command) throws Exception {
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
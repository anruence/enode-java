package org.enodeframework.tests.CommandHandlers;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.applicationmessage.IApplicationMessage;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.tests.Commands.AsyncHandlerCommand;
import org.enodeframework.tests.Commands.TwoAsyncHandlersCommand;

@Command
public class AsyncHandlerCommandHandler {
    public boolean CheckCommandHandledFirst;
    private int _count;

    @Subscribe
    public AsyncTaskResult<IApplicationMessage> HandleAsync(AsyncHandlerCommand command) throws Exception {
        if (command.ShouldGenerateApplicationMessage) {
            return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success, new TestApplicationMessage(command.getAggregateRootId()));
        } else if (command.ShouldThrowException) {
            throw new Exception("AsyncCommandException");
        } else if (command.ShouldThrowIOException) {
            _count++;
            if (_count <= 5) {
                throw new IORuntimeException("AsyncCommandIOException" + _count);
            }
            _count = 0;
            return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success);
        } else {
            return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success);
        }
    }

    @Subscribe
    public AsyncTaskResult<IApplicationMessage> HandleAsync1(TwoAsyncHandlersCommand command) {
        return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success);
    }

    @Subscribe
    public AsyncTaskResult<IApplicationMessage> HandleAsync2(TwoAsyncHandlersCommand command) {
        return new AsyncTaskResult<IApplicationMessage>(AsyncTaskStatus.Success);
    }
}
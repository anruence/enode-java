package org.enodeframework.queue.command;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.ICommand;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public interface ICommandResultProcessor {

    void registerProcessingCommand(ICommand command, CommandReturnType commandReturnType, CompletableFuture<CommandResult> taskCompletionSource);

    InetSocketAddress getBindAddress();

    void processFailedSendingCommand(ICommand command);
}

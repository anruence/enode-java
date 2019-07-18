package com.enodeframework.queue.command;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.commanding.CommandStatus;
import com.enodeframework.commanding.ICommand;
import com.enodeframework.common.exception.ENodeRuntimeException;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.scheduling.Worker;
import com.enodeframework.common.utilities.RemoteReply;
import com.enodeframework.queue.domainevent.DomainEventHandledMessage;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author anruence@gmail.com
 */
public class CommandResultProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CommandResultProcessor.class);

    private InetSocketAddress bindAddress;

    private NetServer netServer;

    private int port = 2019;

    private ConcurrentMap<String, CommandTaskCompletionSource> commandTaskDict;

    private BlockingQueue<CommandResult> commandExecutedMessageLocalQueue;

    private BlockingQueue<DomainEventHandledMessage> domainEventHandledMessageLocalQueue;

    private Worker commandExecutedMessageWorker;

    private Worker domainEventHandledMessageWorker;

    private boolean started;

    public CommandResultProcessor() {
        Vertx vertx = Vertx.vertx();
        netServer = vertx.createNetServer();
        netServer.connectHandler(sock -> {
            sock.endHandler(v -> sock.close()).exceptionHandler(t -> {
                logger.error("Failed to start NetServer", t);
                sock.close();
            }).handler(buffer -> {
                RemoteReply name = buffer.toJsonObject().mapTo(RemoteReply.class);
                processRequestInternal(name);
            });
        });
        commandTaskDict = new ConcurrentHashMap<>();
        commandExecutedMessageLocalQueue = new LinkedBlockingQueue<>();
        domainEventHandledMessageLocalQueue = new LinkedBlockingQueue<>();
        commandExecutedMessageWorker = new Worker("ProcessExecutedCommandMessage", () -> {
            processExecutedCommandMessage(commandExecutedMessageLocalQueue.take());
        });
        domainEventHandledMessageWorker = new Worker("ProcessDomainEventHandledMessage", () -> {
            processDomainEventHandledMessage(domainEventHandledMessageLocalQueue.take());
        });
    }

    public void registerProcessingCommand(ICommand command, CommandReturnType commandReturnType, CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource) {
        if (commandTaskDict.containsKey(command.getId())) {
            throw new ENodeRuntimeException(String.format("Duplicate processing command registration, type:%s, id:%s", command.getClass().getName(), command.getId()));
        }
        commandTaskDict.put(command.getId(), new CommandTaskCompletionSource(commandReturnType, taskCompletionSource));
    }

    public void processFailedSendingCommand(ICommand command) {
        CommandTaskCompletionSource commandTaskCompletionSource = commandTaskDict.remove(command.getId());

        if (commandTaskCompletionSource != null) {
            CommandResult commandResult = new CommandResult(CommandStatus.Failed, command.getId(), command.getAggregateRootId(), "Failed to send the command.", String.class.getName());
            commandTaskCompletionSource.getTaskCompletionSource().complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, commandResult));
        }
    }

    public CommandResultProcessor start() {
        bindAddress = new InetSocketAddress(port);
        if (started) {
            return this;
        }
        netServer.listen(port);
        commandExecutedMessageWorker.start();
        domainEventHandledMessageWorker.start();
        started = true;
        logger.info("Command result processor started, bindAddress: {}", bindAddress);
        return this;
    }

    public CommandResultProcessor shutdown() {
        netServer.close();
        commandExecutedMessageWorker.stop();
        domainEventHandledMessageWorker.stop();
        return this;
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public void processRequestInternal(RemoteReply reply) {
        if (reply.getCode() == CommandReturnType.CommandExecuted.getValue()) {
            CommandResult result = reply.getCommandResult();
            commandExecutedMessageLocalQueue.add(result);
        } else if (reply.getCode() == CommandReturnType.EventHandled.getValue()) {
            DomainEventHandledMessage message = reply.getEventHandledMessage();
            domainEventHandledMessageLocalQueue.add(message);
        } else {
            logger.error("Invalid remoting reply: {}", reply);
        }
    }

    private void processExecutedCommandMessage(CommandResult commandResult) {
        CommandTaskCompletionSource commandTaskCompletionSource = commandTaskDict.get(commandResult.getCommandId());

        if (commandTaskCompletionSource != null) {
            if (commandTaskCompletionSource.getCommandReturnType().equals(CommandReturnType.CommandExecuted)) {
                commandTaskDict.remove(commandResult.getCommandId());

                if (commandTaskCompletionSource.getTaskCompletionSource().complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, commandResult))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Command result return, {}", commandResult);
                    }
                }
            } else if (commandTaskCompletionSource.getCommandReturnType().equals(CommandReturnType.EventHandled)) {
                if (commandResult.getStatus().equals(CommandStatus.Failed) || commandResult.getStatus().equals(CommandStatus.NothingChanged)) {
                    commandTaskDict.remove(commandResult.getCommandId());
                    if (commandTaskCompletionSource.getTaskCompletionSource().complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, commandResult))) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Command result return, {}", commandResult);
                        }
                    }
                }
            }
        }
    }

    private void processDomainEventHandledMessage(DomainEventHandledMessage message) {
        CommandTaskCompletionSource commandTaskCompletionSource = commandTaskDict.remove(message.getCommandId());
        if (commandTaskCompletionSource != null) {
            CommandResult commandResult = new CommandResult(CommandStatus.Success, message.getCommandId(), message.getAggregateRootId(), message.getCommandResult(), message.getCommandResult() != null ? String.class.getName() : null);

            if (commandTaskCompletionSource.getTaskCompletionSource().complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, commandResult))) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Command result return, {}", commandResult);
                }
            }
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    class CommandTaskCompletionSource {
        private CommandReturnType commandReturnType;
        private CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource;

        public CommandTaskCompletionSource(CommandReturnType commandReturnType, CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource) {
            this.commandReturnType = commandReturnType;
            this.taskCompletionSource = taskCompletionSource;
        }

        public CommandReturnType getCommandReturnType() {
            return commandReturnType;
        }

        public void setCommandReturnType(CommandReturnType commandReturnType) {
            this.commandReturnType = commandReturnType;
        }

        public CompletableFuture<AsyncTaskResult<CommandResult>> getTaskCompletionSource() {
            return taskCompletionSource;
        }

        public void setTaskCompletionSource(CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource) {
            this.taskCompletionSource = taskCompletionSource;
        }
    }
}

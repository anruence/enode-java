package com.enode.message;

import com.enode.commanding.CommandResult;
import com.enode.commanding.CommandReturnType;
import com.enode.commanding.CommandStatus;
import com.enode.commanding.ICommand;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.remoting.RemotingServer;
import com.enode.common.remoting.netty.NettyRemotingServer;
import com.enode.common.remoting.netty.NettyRequestProcessor;
import com.enode.common.remoting.netty.NettyServerConfig;
import com.enode.common.remoting.protocol.RemotingCommand;
import com.enode.common.scheduling.Worker;
import com.enode.common.serializing.IJsonSerializer;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandResultProcessor implements NettyRequestProcessor {
    private static final Logger _logger = ENodeLogger.getLog();

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    public SocketAddress _bindingAddress;
    private RemotingServer remotingServer;
    private ConcurrentMap<String, CommandTaskCompletionSource> _commandTaskDict;
    private BlockingQueue<CommandResult> _commandExecutedMessageLocalQueue;
    private BlockingQueue<DomainEventHandledMessage> _domainEventHandledMessageLocalQueue;
    private Worker _commandExecutedMessageWorker;
    private Worker _domainEventHandledMessageWorker;
    private IJsonSerializer _jsonSerializer;
    private boolean _started;

    public CommandResultProcessor(int listenPort, IJsonSerializer jsonSerializer) {
        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setListenPort(listenPort);
        nettyServerConfig.setServerChannelMaxIdleTimeSeconds(3600);
        remotingServer = new NettyRemotingServer(nettyServerConfig);
        remotingServer.registerProcessor(CommandReplyType.CommandExecuted.getValue(), this);
        remotingServer.registerProcessor(CommandReplyType.DomainEventHandled.getValue(), this);

        _commandTaskDict = new ConcurrentHashMap<>();
        _commandExecutedMessageLocalQueue = new LinkedBlockingQueue<>();
        _domainEventHandledMessageLocalQueue = new LinkedBlockingQueue<>();
        _commandExecutedMessageWorker = new Worker("ProcessExecutedCommandMessage", () -> processExecutedCommandMessage(_commandExecutedMessageLocalQueue.take()));
        _domainEventHandledMessageWorker = new Worker("ProcessDomainEventHandledMessage", () -> processDomainEventHandledMessage(_domainEventHandledMessageLocalQueue.take()));
        _jsonSerializer = jsonSerializer;
    }

    public void registerProcessingCommand(ICommand command, CommandReturnType commandReturnType, CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource) {
        if (_commandTaskDict.containsKey(command.id())) {
            throw new RuntimeException(String.format("Duplicate processing command registration, type:%s, id:%s", command.getClass().getName(), command.id()));
        }

        _commandTaskDict.put(command.id(), new CommandTaskCompletionSource(commandReturnType, taskCompletionSource));
    }

    public void processFailedSendingCommand(ICommand command) {
        CommandTaskCompletionSource commandTaskCompletionSource = _commandTaskDict.remove(command.id());

        if (commandTaskCompletionSource != null) {
            CommandResult commandResult = new CommandResult(CommandStatus.Failed, command.id(), command.getAggregateRootId(), "Failed to send the command.", String.class.getName());
            commandTaskCompletionSource.getTaskCompletionSource().complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, commandResult));
        }
    }

    public CommandResultProcessor start() {
        if (_started) {
            return this;
        }

        remotingServer.start();
        _bindingAddress = remotingServer.bindAddress();

        _commandExecutedMessageWorker.start();
        _domainEventHandledMessageWorker.start();

        _started = true;

        _logger.info("Command result processor started, bindingAddress: {}", remotingServer.bindAddress());

        return this;
    }

    public CommandResultProcessor shutdown() {
        remotingServer.shutdown();
        _commandExecutedMessageWorker.stop();
        _domainEventHandledMessageWorker.stop();
        return this;
    }

    public SocketAddress getBindingAddress() {
        return _bindingAddress;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
        if (request.getCode() == CommandReplyType.CommandExecuted.getValue()) {
            String body = new String(request.getBody(), CHARSET_UTF8);
            CommandResult result = _jsonSerializer.deserialize(body, CommandResult.class);
            _commandExecutedMessageLocalQueue.add(result);
        } else if (request.getCode() == CommandReplyType.DomainEventHandled.getValue()) {
            String body = new String(request.getBody(), CHARSET_UTF8);
            DomainEventHandledMessage message = _jsonSerializer.deserialize(body, DomainEventHandledMessage.class);
            _domainEventHandledMessageLocalQueue.add(message);
        } else {
            _logger.error("Invalid remoting request: {}", request);
        }
        return null;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }

    private void processExecutedCommandMessage(CommandResult commandResult) {
        CommandTaskCompletionSource commandTaskCompletionSource = _commandTaskDict.get(commandResult.getCommandId());

        if (commandTaskCompletionSource != null) {
            if (commandTaskCompletionSource.getCommandReturnType().equals(CommandReturnType.CommandExecuted)) {
                _commandTaskDict.remove(commandResult.getCommandId());

                if (commandTaskCompletionSource.getTaskCompletionSource().complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, commandResult))) {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Command result return, {}", commandResult);
                    }
                }
            } else if (commandTaskCompletionSource.getCommandReturnType().equals(CommandReturnType.EventHandled)) {
                if (commandResult.getStatus().equals(CommandStatus.Failed) || commandResult.getStatus().equals(CommandStatus.NothingChanged)) {
                    _commandTaskDict.remove(commandResult.getCommandId());
                    if (commandTaskCompletionSource.getTaskCompletionSource().complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, commandResult))) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug("Command result return, {}", commandResult);
                        }
                    }
                }
            }
        }
    }

    private void processDomainEventHandledMessage(DomainEventHandledMessage message) {
        CommandTaskCompletionSource commandTaskCompletionSource = _commandTaskDict.remove(message.getCommandId());
        if (commandTaskCompletionSource != null) {
            CommandResult commandResult = new CommandResult(CommandStatus.Success, message.getCommandId(), message.getAggregateRootId(), message.getCommandResult(), message.getCommandResult() != null ? String.class.getName() : null);

            if (commandTaskCompletionSource.getTaskCompletionSource().complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, commandResult))) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Command result return, {}", commandResult);
                }
            }
        }
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

package org.enodeframework.queue.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.common.SysProperties;
import org.enodeframework.common.exception.DuplicateRegisterException;
import org.enodeframework.common.scheduling.IScheduleService;
import org.enodeframework.common.scheduling.Worker;
import org.enodeframework.common.utilities.RemoteReply;
import org.enodeframework.queue.domainevent.DomainEventHandledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author anruence@gmail.com
 */
public class DefaultCommandResultProcessor extends AbstractVerticle implements ICommandResultProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCommandResultProcessor.class);
    private final int port;
    private final String scanExpireCommandTaskName;
    private final IScheduleService scheduleService;
    private final int completionSourceTimeout;
    private final Cache<String, CommandTaskCompletionSource> commandTaskDict;
    private final BlockingQueue<CommandResult> commandExecutedMessageLocalQueue;
    private final BlockingQueue<DomainEventHandledMessage> domainEventHandledMessageLocalQueue;
    private final Worker commandExecutedMessageWorker;
    private final Worker domainEventHandledMessageWorker;

    private InetSocketAddress bindAddress;
    private NetServer netServer;
    private boolean started;

    public DefaultCommandResultProcessor(IScheduleService scheduleService, int port) {
        this(scheduleService, port, SysProperties.COMPLETION_SOURCE_TIMEOUT);
    }

    public DefaultCommandResultProcessor(IScheduleService scheduleService, int port, int completionSourceTimeout) {
        this.scheduleService = scheduleService;
        this.port = port;
        this.completionSourceTimeout = completionSourceTimeout;
        this.scanExpireCommandTaskName = "CleanTimeoutCommandTask_" + System.currentTimeMillis() + new Random().nextInt(10000);
        this.commandTaskDict = CacheBuilder.newBuilder().removalListener((RemovalListener<String, CommandTaskCompletionSource>) notification -> {
            if (notification.getCause().equals(RemovalCause.EXPIRED)) {
                processTimeoutCommand(notification.getKey(), notification.getValue());
            }
        }).expireAfterWrite(completionSourceTimeout, TimeUnit.MILLISECONDS).build();
        this.commandExecutedMessageLocalQueue = new LinkedBlockingQueue<>();
        this.domainEventHandledMessageLocalQueue = new LinkedBlockingQueue<>();
        this.commandExecutedMessageWorker = new Worker("ProcessExecutedCommandMessage", () -> {
            processExecutedCommandMessage(commandExecutedMessageLocalQueue.take());
        });
        this.domainEventHandledMessageWorker = new Worker("ProcessDomainEventHandledMessage", () -> {
            processDomainEventHandledMessage(domainEventHandledMessageLocalQueue.take());
        });
    }

    public void startServer(int port) {
        netServer = vertx.createNetServer();
        netServer.connectHandler(sock -> {
            RecordParser parser = RecordParser.newDelimited(SysProperties.DELIMITED, sock);
            parser.endHandler(v -> sock.close()).exceptionHandler(t -> {
                logger.error("Failed to start NetServer port:{}", port, t);
                sock.close();
            }).handler(buffer -> {
                RemoteReply name = buffer.toJsonObject().mapTo(RemoteReply.class);
                processRequestInternal(name);
            });
        });
        bindAddress = new InetSocketAddress(port);
        netServer.listen(port);
    }

    @Override
    public void registerProcessingCommand(ICommand command, CommandReturnType commandReturnType, CompletableFuture<CommandResult> taskCompletionSource) {
        if (commandTaskDict.asMap().containsKey(command.getId())) {
            throw new DuplicateRegisterException(String.format("Duplicate processing command registration, type:%s, id:%s", command.getClass().getName(), command.getId()));
        }
        commandTaskDict.asMap().put(command.getId(), new CommandTaskCompletionSource(command.getAggregateRootId(), commandReturnType, taskCompletionSource));
    }

    @Override
    public void start() {
        if (started) {
            return;
        }
        startServer(port);
        commandExecutedMessageWorker.start();
        domainEventHandledMessageWorker.start();
        scheduleService.startTask(scanExpireCommandTaskName, commandTaskDict::cleanUp, completionSourceTimeout, completionSourceTimeout);
        started = true;
    }

    @Override
    public void stop() {
        scheduleService.stopTask(scanExpireCommandTaskName);
        commandExecutedMessageWorker.stop();
        domainEventHandledMessageWorker.stop();
        netServer.close();
    }

    @Override
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

    /**
     * https://stackoverflow.com/questions/10626720/guava-cachebuilder-removal-listener
     * Caches built with CacheBuilder do not perform cleanup and evict values "automatically," or instantly
     * after a value expires, or anything of the sort. Instead, it performs small amounts of maintenance
     * during write operations, or during occasional read operations if writes are rare.
     * <p>
     * The reason for this is as follows: if we wanted to perform Cache maintenance continuously, we would need
     * to create a thread, and its operations would be competing with user operations for shared locks.
     * Additionally, some environments restrict the creation of threads, which would make CacheBuilder unusable in that environment.
     */
    private void processExecutedCommandMessage(CommandResult commandResult) {
        CommandTaskCompletionSource commandTaskCompletionSource = commandTaskDict.asMap().get(commandResult.getCommandId());
        // 主动触发cleanUp
        commandTaskDict.cleanUp();
        if (commandTaskCompletionSource == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Command result return, {}, but commandTaskCompletionSource maybe timeout expired.", commandResult);
            }
            return;
        }
        if (commandTaskCompletionSource.getCommandReturnType().equals(CommandReturnType.CommandExecuted)) {
            commandTaskDict.asMap().remove(commandResult.getCommandId());
            if (commandTaskCompletionSource.getTaskCompletionSource().complete(commandResult)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Command result return CommandExecuted, {}", commandResult);
                }
            }
        } else if (commandTaskCompletionSource.getCommandReturnType().equals(CommandReturnType.EventHandled)) {
            if (commandResult.getStatus().equals(CommandStatus.Failed) || commandResult.getStatus().equals(CommandStatus.NothingChanged)) {
                commandTaskDict.asMap().remove(commandResult.getCommandId());
                if (commandTaskCompletionSource.getTaskCompletionSource().complete(commandResult)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Command result return EventHandled, {}", commandResult);
                    }
                }
            }
        }
    }

    private void processTimeoutCommand(String commandId, CommandTaskCompletionSource commandTaskCompletionSource) {
        if (commandTaskCompletionSource != null) {
            logger.error("Wait command notify timeout, commandId:{}", commandId);
            CommandResult commandResult = new CommandResult(CommandStatus.Failed, commandId, commandTaskCompletionSource.getAggregateRootId(), "Wait command notify timeout.", String.class.getName());
            // 任务超时失败
            commandTaskCompletionSource.getTaskCompletionSource().complete(commandResult);
        }
    }

    @Override
    public void processFailedSendingCommand(ICommand command) {
        CommandTaskCompletionSource commandTaskCompletionSource = commandTaskDict.asMap().remove(command.getId());
        if (commandTaskCompletionSource != null) {
            CommandResult commandResult = new CommandResult(CommandStatus.Failed, command.getId(), command.getAggregateRootId(), "Failed to send the command.", String.class.getName());
            // 发送失败消息
            commandTaskCompletionSource.getTaskCompletionSource().complete(commandResult);
        }
    }

    private void processDomainEventHandledMessage(DomainEventHandledMessage message) {
        CommandTaskCompletionSource commandTaskCompletionSource = commandTaskDict.asMap().remove(message.getCommandId());
        if (commandTaskCompletionSource != null) {
            CommandResult commandResult = new CommandResult(CommandStatus.Success, message.getCommandId(), message.getAggregateRootId(), message.getCommandResult(), message.getCommandResult() != null ? String.class.getName() : null);
            if (commandTaskCompletionSource.getTaskCompletionSource().complete(commandResult)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Command result return, {}", commandResult);
                }
            }
        }
    }
}

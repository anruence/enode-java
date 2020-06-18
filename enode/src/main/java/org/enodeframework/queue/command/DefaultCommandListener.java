package org.enodeframework.queue.command;

import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandProcessor;
import org.enodeframework.commanding.ProcessingCommand;
import org.enodeframework.commanding.impl.CommandExecuteContext;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.domain.IAggregateStorage;
import org.enodeframework.domain.IRepository;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.ISendReplyService;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultCommandListener implements IMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCommandListener.class);

    private final ISendReplyService sendReplyService;

    private final ITypeNameProvider typeNameProvider;

    private final ICommandProcessor commandProcessor;

    private final IRepository repository;

    private final IAggregateStorage aggregateRootStorage;

    public DefaultCommandListener(ISendReplyService sendReplyService, ITypeNameProvider typeNameProvider, ICommandProcessor commandProcessor, IRepository repository, IAggregateStorage aggregateRootStorage) {
        this.sendReplyService = sendReplyService;
        this.typeNameProvider = typeNameProvider;
        this.commandProcessor = commandProcessor;
        this.repository = repository;
        this.aggregateRootStorage = aggregateRootStorage;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        Map<String, String> commandItems = new HashMap<>();
        CommandMessage commandMessage = JsonTool.deserialize(queueMessage.getBody(), CommandMessage.class);
        Class<?> commandType = typeNameProvider.getType(commandMessage.getCommandType());
        ICommand command = (ICommand) JsonTool.deserialize(commandMessage.getCommandData(), commandType);
        CommandExecuteContext commandExecuteContext = new CommandExecuteContext(repository, aggregateRootStorage, queueMessage, context, commandMessage, sendReplyService);
        commandItems.put("CommandReplyAddress", commandMessage.getReplyAddress());
        if (logger.isDebugEnabled()) {
            logger.debug("Enode command message received, messageId: {}, aggregateRootId: {}", command.getId(), command.getAggregateRootId());
        }
        commandProcessor.process(new ProcessingCommand(command, commandExecuteContext, commandItems));
    }
}

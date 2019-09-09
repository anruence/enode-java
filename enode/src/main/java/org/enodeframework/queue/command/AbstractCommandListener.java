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
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendReplyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCommandListener implements IMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractCommandListener.class);
    @Autowired
    protected SendReplyService sendReplyService;
    @Autowired
    protected ITypeNameProvider typeNameProvider;
    @Autowired
    protected ICommandProcessor commandProcessor;
    @Autowired
    protected IRepository repository;
    @Autowired
    protected IAggregateStorage aggregateRootStorage;

    public AbstractCommandListener setSendReplyService(SendReplyService sendReplyService) {
        this.sendReplyService = sendReplyService;
        return this;
    }

    public AbstractCommandListener setTypeNameProvider(ITypeNameProvider typeNameProvider) {
        this.typeNameProvider = typeNameProvider;
        return this;
    }

    public AbstractCommandListener setCommandProcessor(ICommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
        return this;
    }

    public AbstractCommandListener setRepository(IRepository repository) {
        this.repository = repository;
        return this;
    }

    public AbstractCommandListener setAggregateRootStorage(IAggregateStorage aggregateRootStorage) {
        this.aggregateRootStorage = aggregateRootStorage;
        return this;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        Map<String, String> commandItems = new HashMap<>();
        CommandMessage commandMessage = JsonTool.deserialize(queueMessage.getBody(), CommandMessage.class);
        Class commandType = typeNameProvider.getType(commandMessage.getCommandType());
        ICommand command = (ICommand) JsonTool.deserialize(commandMessage.getCommandData(), commandType);
        CommandExecuteContext commandExecuteContext = new CommandExecuteContext(repository, aggregateRootStorage, queueMessage, context, commandMessage, sendReplyService);
        commandItems.put("CommandReplyAddress", commandMessage.getReplyAddress());
        if (logger.isDebugEnabled()) {
            logger.debug("ENode command message received, messageId: {}, aggregateRootId: {}", command.getId(), command.getAggregateRootId());
        }
        commandProcessor.process(new ProcessingCommand(command, commandExecuteContext, commandItems));
    }
}

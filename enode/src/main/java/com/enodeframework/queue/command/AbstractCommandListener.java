package com.enodeframework.queue.command;

import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandProcessor;
import com.enodeframework.commanding.ProcessingCommand;
import com.enodeframework.commanding.impl.CommandExecuteContext;
import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.domain.IAggregateStorage;
import com.enodeframework.domain.IRepository;
import com.enodeframework.infrastructure.ITypeNameProvider;
import com.enodeframework.queue.IMessageContext;
import com.enodeframework.queue.IMessageHandler;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.SendReplyService;
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

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        Map<String, String> commandItems = new HashMap<>();
        CommandMessage commandMessage = JsonTool.deserialize(queueMessage.getBody(), CommandMessage.class);
        Class commandType = typeNameProvider.getType(commandMessage.getCommandType());
        ICommand command = (ICommand) JsonTool.deserialize(commandMessage.getCommandData(), commandType);
        CommandExecuteContext commandExecuteContext = new CommandExecuteContext(repository, aggregateRootStorage, queueMessage, context, commandMessage, sendReplyService);
        commandItems.put("CommandReplyAddress", commandMessage.getReplyAddress());
        logger.info("ENode command message received, messageId: {}, aggregateRootId: {}", command.id(), command.getAggregateRootId());
        commandProcessor.process(new ProcessingCommand(command, commandExecuteContext, commandItems));
    }
}

package org.enodeframework.queue.command;

import com.google.common.base.Strings;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandProcessor;
import org.enodeframework.commanding.ProcessingCommand;
import org.enodeframework.commanding.impl.CommandExecuteContext;
import org.enodeframework.common.SysProperties;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.common.utilities.InetUtil;
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

public class DefaultCommandMessageHandler implements IMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCommandMessageHandler.class);

    private final ISendReplyService sendReplyService;

    private final ITypeNameProvider typeNameProvider;

    private final ICommandProcessor commandProcessor;

    private final IRepository repository;

    private final IAggregateStorage aggregateRootStorage;

    private final ISerializeService serializeService;

    public DefaultCommandMessageHandler(ISendReplyService sendReplyService, ITypeNameProvider typeNameProvider, ICommandProcessor commandProcessor, IRepository repository, IAggregateStorage aggregateRootStorage, ISerializeService serializeService) {
        this.sendReplyService = sendReplyService;
        this.typeNameProvider = typeNameProvider;
        this.commandProcessor = commandProcessor;
        this.repository = repository;
        this.aggregateRootStorage = aggregateRootStorage;
        this.serializeService = serializeService;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        logger.info("Received command message: {}", serializeService.serialize(queueMessage));
        CommandMessage commandMessage = serializeService.deserialize(queueMessage.getBody(), CommandMessage.class);
        Class<?> commandType = typeNameProvider.getType(commandMessage.getCommandType());
        ICommand command = (ICommand) serializeService.deserialize(commandMessage.getCommandData(), commandType);
        CommandExecuteContext commandExecuteContext = new CommandExecuteContext(repository, aggregateRootStorage, queueMessage, context, commandMessage, sendReplyService);
        Map<String, Object> commandItems = new HashMap<>();
        String uri = InetUtil.toUri(commandMessage.getReplyAddress());
        if (!Strings.isNullOrEmpty(uri)) {
            commandItems.put(SysProperties.ITEMS_COMMAND_REPLY_ADDRESS_KEY, uri);
        }
        commandProcessor.process(new ProcessingCommand(command, commandExecuteContext, commandItems));
    }
}

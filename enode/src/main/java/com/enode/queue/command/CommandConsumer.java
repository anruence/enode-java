package com.enode.queue.command;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandProcessor;
import com.enode.commanding.ProcessingCommand;
import com.enode.commanding.impl.CommandExecuteContext;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.domain.IAggregateStorage;
import com.enode.domain.IRepository;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.queue.IMessageContext;
import com.enode.queue.IMessageHandler;
import com.enode.queue.QueueMessage;
import com.enode.queue.SendReplyService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class CommandConsumer implements IMessageHandler {

    private static final Logger _logger = ENodeLogger.getLog();

    protected String defaultMessageConsumerGroup = "CommandConsumerGroup";

    @Autowired
    protected SendReplyService _sendReplyService;

    @Autowired
    protected IJsonSerializer _jsonSerializer;

    @Autowired
    protected ITypeNameProvider _typeNameProvider;

    @Autowired
    protected ICommandProcessor _processor;

    @Autowired
    protected IRepository _repository;

    @Autowired
    protected IAggregateStorage _aggregateRootStorage;

    public CommandConsumer start() {
        _sendReplyService.start();
        return this;
    }

    public CommandConsumer shutdown() {
        _sendReplyService.stop();
        return this;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        Map<String, String> commandItems = new HashMap<>();
        CommandMessage commandMessage = _jsonSerializer.deserialize(queueMessage.getBody(), CommandMessage.class);
        Class commandType = _typeNameProvider.getType(commandMessage.getCommandType());
        ICommand command = (ICommand) _jsonSerializer.deserialize(commandMessage.getCommandData(), commandType);
        CommandExecuteContext commandExecuteContext = new CommandExecuteContext(_repository, _aggregateRootStorage, queueMessage, context, commandMessage, _sendReplyService);
        commandItems.put("CommandReplyAddress", commandMessage.getReplyAddress());
        _logger.info("ENode command message received, messageId: {}, aggregateRootId: {}", command.id(), command.getAggregateRootId());
        _processor.process(new ProcessingCommand(command, commandExecuteContext, commandItems));
    }
}

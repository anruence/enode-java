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
import com.enode.queue.IMQConsumer;
import com.enode.queue.TopicData;
import com.enode.queue.CompletableConsumeConcurrentlyContext;
import com.enode.queue.IMQMessageHandler;
import com.enode.queue.ITopicProvider;
import com.enode.queue.SendReplyService;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommandConsumer {
    private static final Logger _logger = ENodeLogger.getLog();

    private final IMQConsumer _consumer;
    private final SendReplyService _sendReplyService;
    private final IJsonSerializer _jsonSerializer;
    private final ITypeNameProvider _typeNameProvider;
    private final ICommandProcessor _processor;
    private final IRepository _repository;
    private final IAggregateStorage _aggregateRootStorage;
    private final ITopicProvider<ICommand> _commandTopicProvider;

    @Inject
    public CommandConsumer(
            IMQConsumer consumer, IJsonSerializer jsonSerializer, ITypeNameProvider typeNameProvider,
            ICommandProcessor commandProcessor, IRepository repository,
            IAggregateStorage aggregateStorage, ITopicProvider<ICommand> commandTopicProvider,
            SendReplyService sendReplyService) {
        _consumer = consumer;
        _sendReplyService = sendReplyService;
        _jsonSerializer = jsonSerializer;
        _typeNameProvider = typeNameProvider;
        _processor = commandProcessor;
        _repository = repository;
        _aggregateRootStorage = aggregateStorage;
        _commandTopicProvider = commandTopicProvider;
    }

    public CommandConsumer start() {
        _consumer.registerMessageHandler(new CommandMQMessageHandler());
        _sendReplyService.start();
        return this;
    }

    public CommandConsumer shutdown() {
        _sendReplyService.stop();
        return this;
    }

    class CommandMQMessageHandler implements IMQMessageHandler {

        @Override
        public boolean isMatched(TopicData topicTagData) {
            return _commandTopicProvider.getAllSubscribeTopics().contains(topicTagData);
        }

        @Override
        public void handle(String msg, CompletableConsumeConcurrentlyContext context) {
            Map<String, String> commandItems = new HashMap<>();
            CommandMessage commandMessage = _jsonSerializer.deserialize(msg, CommandMessage.class);
            Class commandType = _typeNameProvider.getType(commandMessage.getCommandType());
            ICommand command = (ICommand) _jsonSerializer.deserialize(commandMessage.getCommandData(), commandType);
            CompletableFuture consumeResultFuture = new CompletableFuture();
            CommandExecuteContext commandExecuteContext = new CommandExecuteContext(_repository, _aggregateRootStorage, msg, context, commandMessage, _sendReplyService, consumeResultFuture);
            commandItems.put("CommandReplyAddress", commandMessage.getReplyAddress());
            _logger.info("ENode command message received, messageId: {}, aggregateRootId: {}", command.id(), command.getAggregateRootId());
            _processor.process(new ProcessingCommand(command, commandExecuteContext, commandItems));
        }
    }
}

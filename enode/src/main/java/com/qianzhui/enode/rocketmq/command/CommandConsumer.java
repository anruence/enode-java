package com.qianzhui.enode.rocketmq.command;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.qianzhui.enode.commanding.AggregateRootAlreadyExistException;
import com.qianzhui.enode.commanding.CommandResult;
import com.qianzhui.enode.commanding.ICommand;
import com.qianzhui.enode.commanding.ICommandExecuteContext;
import com.qianzhui.enode.commanding.ICommandProcessor;
import com.qianzhui.enode.commanding.ProcessingCommand;
import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enode.common.rocketmq.consumer.listener.CompletableConsumeConcurrentlyContext;
import com.qianzhui.enode.common.serializing.IJsonSerializer;
import com.qianzhui.enode.common.utilities.BitConverter;
import com.qianzhui.enode.domain.IAggregateRoot;
import com.qianzhui.enode.domain.IAggregateStorage;
import com.qianzhui.enode.domain.IRepository;
import com.qianzhui.enode.infrastructure.ITypeNameProvider;
import com.qianzhui.enode.rocketmq.CommandReplyType;
import com.qianzhui.enode.rocketmq.ITopicProvider;
import com.qianzhui.enode.rocketmq.RocketMQConsumer;
import com.qianzhui.enode.rocketmq.RocketMQMessageHandler;
import com.qianzhui.enode.rocketmq.SendReplyService;
import com.qianzhui.enode.rocketmq.TopicTagData;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandConsumer {
    private static final Logger _logger = ENodeLogger.getLog();

    private final RocketMQConsumer _consumer;
    private final SendReplyService _sendReplyService;
    private final IJsonSerializer _jsonSerializer;
    private final ITypeNameProvider _typeNameProvider;
    private final ICommandProcessor _processor;
    private final IRepository _repository;
    private final IAggregateStorage _aggregateRootStorage;
    private final ITopicProvider<ICommand> _commandTopicProvider;

    public RocketMQConsumer getConsumer() {
        return _consumer;
    }

    @Inject
    public CommandConsumer(
            RocketMQConsumer consumer, IJsonSerializer jsonSerializer, ITypeNameProvider typeNameProvider,
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
        _consumer.registerMessageHandler(new RocketMQMessageHandler() {
            @Override
            public boolean isMatched(TopicTagData topicTagData) {
                return _commandTopicProvider.getAllSubscribeTopics().contains(topicTagData);
            }

            @Override
            public void handle(MessageExt message, CompletableConsumeConcurrentlyContext context) {
                CommandConsumer.this.handle(message, context);
            }
        });

        _sendReplyService.start();
        return this;
    }

    public CommandConsumer shutdown() {
        _sendReplyService.stop();
        return this;
    }

    //TODO consume ack
    void handle(final MessageExt msg, final CompletableConsumeConcurrentlyContext context) {
        Map<String, String> commandItems = new HashMap<>();
        CommandMessage commandMessage = _jsonSerializer.deserialize(BitConverter.toString(msg.getBody()), CommandMessage.class);
        Class commandType = _typeNameProvider.getType(commandMessage.getCommandType());
        ICommand command = (ICommand) _jsonSerializer.deserialize(commandMessage.getCommandData(), commandType);
        CompletableFuture<ConsumeConcurrentlyStatus> consumeResultFuture = new CompletableFuture<>();
        CommandExecuteContext commandExecuteContext = new CommandExecuteContext(_repository, _aggregateRootStorage, msg, context, commandMessage, _sendReplyService, consumeResultFuture);
        commandItems.put("CommandReplyAddress", commandMessage.getReplyAddress());
        _logger.info("ENode command message received, messageId: {}, aggregateRootId: {}", command.id(), command.getAggregateRootId());
        _processor.process(new ProcessingCommand(command, commandExecuteContext, commandItems));
    }

    class CommandExecuteContext implements ICommandExecuteContext {
        private String _result;
        private final ConcurrentMap<String, IAggregateRoot> _trackingAggregateRootDict;
        private final IRepository _repository;
        private final IAggregateStorage _aggregateRootStorage;
        private final SendReplyService _sendReplyService;
        private final Object _queueMessage;
        private CompletableConsumeConcurrentlyContext _messageContext;
        private CompletableFuture<ConsumeConcurrentlyStatus> _consumeResultFuture;
        private CommandMessage _commandMessage;

        public CommandExecuteContext(IRepository repository, IAggregateStorage aggregateRootStorage, Object queueMessage, CompletableConsumeConcurrentlyContext messageContext,
                                     CommandMessage commandMessage, SendReplyService sendReplyService, CompletableFuture<ConsumeConcurrentlyStatus> consumeResultFuture) {
            _trackingAggregateRootDict = new ConcurrentHashMap<>();
            _repository = repository;
            _aggregateRootStorage = aggregateRootStorage;
            _sendReplyService = sendReplyService;
            _queueMessage = queueMessage;
            _commandMessage = commandMessage;
            _messageContext = messageContext;
            _consumeResultFuture = consumeResultFuture;
        }

        @Override
        public CompletableFuture onCommandExecutedAsync(CommandResult commandResult) {
            _messageContext.onMessageHandled();
            _consumeResultFuture.complete(ConsumeConcurrentlyStatus.CONSUME_SUCCESS);

            if (_commandMessage.getReplyAddress() == null) {
                return CompletableFuture.completedFuture(null);
            }
            return _sendReplyService.sendReply(CommandReplyType.CommandExecuted.getValue(), commandResult, _commandMessage.getReplyAddress());
        }

        @Override
        public void add(IAggregateRoot aggregateRoot) {
            if (aggregateRoot == null) {
                throw new NullPointerException("aggregateRoot");
            }

            if (_trackingAggregateRootDict.containsKey(aggregateRoot.uniqueId())) {
                throw new AggregateRootAlreadyExistException(aggregateRoot.uniqueId(), aggregateRoot.getClass());
            }

            _trackingAggregateRootDict.put(aggregateRoot.uniqueId(), aggregateRoot);
        }

        /**
         * Add a new aggregate into the current command context synchronously, and then return a completed task object.
         *
         * @param aggregateRoot
         * @return
         */
        @Override
        public CompletableFuture addAsync(IAggregateRoot aggregateRoot) {
            return CompletableFuture.supplyAsync(() -> {
                add(aggregateRoot);
                return true;
            });
        }

        /**
         * Get an aggregate from the current command context.
         *
         * @param id
         * @param firstFromCache
         * @return
         */
        @Override
        public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object id, boolean firstFromCache, Class<T> aggregateRootType) {
            if (id == null) {
                throw new NullPointerException("id");
            }
            String aggregateRootId = id.toString();
            T iAggregateRoot = (T) _trackingAggregateRootDict.get(aggregateRootId);
            CompletableFuture<T> future = new CompletableFuture<>();
            if (iAggregateRoot != null) {
                future.complete(iAggregateRoot);
                return future;
            }
            if (firstFromCache) {
                future = _repository.getAsync(aggregateRootType, id);
            } else {
                future = _aggregateRootStorage.getAsync(aggregateRootType, aggregateRootId);
            }
            return future.thenApply(aggregateRoot -> {
                if (aggregateRoot != null) {
                    _trackingAggregateRootDict.putIfAbsent(aggregateRoot.uniqueId(), aggregateRoot);
                }
                return aggregateRoot;
            });
        }

        @Override
        public List<IAggregateRoot> getTrackedAggregateRoots() {
            return new ArrayList<>(_trackingAggregateRootDict.values());
        }

        @Override
        public void clear() {
            _trackingAggregateRootDict.clear();
            _result = null;
        }

        @Override
        public void setResult(String result) {
            _result = result;
        }

        @Override
        public String getResult() {
            return _result;
        }
    }
}

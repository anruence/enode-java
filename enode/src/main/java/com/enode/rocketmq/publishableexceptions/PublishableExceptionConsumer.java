package com.enode.rocketmq.publishableexceptions;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.BitConverter;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.IPublishableException;
import com.enode.infrastructure.ISequenceMessage;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.ProcessingPublishableExceptionMessage;
import com.enode.infrastructure.WrappedRuntimeException;
import com.enode.infrastructure.impl.DefaultMessageProcessContext;
import com.enode.rocketmq.ITopicProvider;
import com.enode.rocketmq.TopicTagData;
import com.enode.rocketmq.client.IMQMessageHandler;
import com.enode.rocketmq.client.RocketMQConsumer;
import com.enode.rocketmq.client.consumer.listener.CompletableConsumeConcurrentlyContext;
import org.slf4j.Logger;

import javax.inject.Inject;


public class PublishableExceptionConsumer {
    private static final Logger _logger = ENodeLogger.getLog();

    private final RocketMQConsumer _consumer;
    private final IJsonSerializer _jsonSerializer;
    private final ITopicProvider<IPublishableException> _exceptionTopicProvider;
    private final ITypeNameProvider _typeNameProvider;
    private final IMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException> _publishableExceptionProcessor;

    @Inject
    public PublishableExceptionConsumer(RocketMQConsumer consumer, IJsonSerializer jsonSerializer,
                                        ITopicProvider<IPublishableException> exceptionITopicProvider, ITypeNameProvider typeNameProvider,
                                        IMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException> publishableExceptionProcessor) {
        _consumer = consumer;
        _jsonSerializer = jsonSerializer;
        _exceptionTopicProvider = exceptionITopicProvider;
        _typeNameProvider = typeNameProvider;
        _publishableExceptionProcessor = publishableExceptionProcessor;
    }

    public PublishableExceptionConsumer start() {
        _consumer.registerMessageHandler(new PublishableExceptionMessageHandle());
        return this;
    }

    public PublishableExceptionConsumer shutdown() {
        return this;
    }

    class PublishableExceptionMessageHandle implements IMQMessageHandler {
        @Override
        public boolean isMatched(TopicTagData topicTagData) {
            return _exceptionTopicProvider.getAllSubscribeTopics().contains(topicTagData);
        }

        @Override
        public void handle(Object msg, CompletableConsumeConcurrentlyContext context) {
            PublishableExceptionMessage exceptionMessage = _jsonSerializer.deserialize(msg.toString(), PublishableExceptionMessage.class);
            Class exceptionType = _typeNameProvider.getType(exceptionMessage.getExceptionType());

            IPublishableException exception;

            try {
                exception = (IPublishableException) exceptionType.getConstructor().newInstance();
            } catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
            exception.setId(exceptionMessage.getUniqueId());
            exception.setTimestamp(exceptionMessage.getTimestamp());
            exception.restoreFrom(exceptionMessage.getSerializableInfo());

            if (exception instanceof ISequenceMessage) {
                ISequenceMessage sequenceMessage = (ISequenceMessage) exception;
                sequenceMessage.setAggregateRootTypeName(exceptionMessage.getAggregateRootTypeName());
                sequenceMessage.setAggregateRootStringId(exceptionMessage.getAggregateRootId());
            }

            DefaultMessageProcessContext processContext = new DefaultMessageProcessContext(msg, context);
            ProcessingPublishableExceptionMessage processingMessage = new ProcessingPublishableExceptionMessage(exception, processContext);
            _logger.info("ENode exception message received, messageId: {}, aggregateRootId: {}, aggregateRootType: {}", exceptionMessage.getUniqueId(), exceptionMessage.getAggregateRootId(), exceptionMessage.getAggregateRootTypeName());
            _publishableExceptionProcessor.process(processingMessage);
        }
    }
}

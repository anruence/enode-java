package com.qianzhui.enode.rocketmq.publishableexceptions;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enode.common.rocketmq.consumer.listener.CompletableConsumeConcurrentlyContext;
import com.qianzhui.enode.common.serializing.IJsonSerializer;
import com.qianzhui.enode.common.utilities.BitConverter;
import com.qianzhui.enode.infrastructure.*;
import com.qianzhui.enode.rocketmq.*;
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
        _consumer.registerMessageHandler(new RocketMQMessageHandler() {
            @Override
            public boolean isMatched(TopicTagData topicTagData) {
                return _exceptionTopicProvider.getAllSubscribeTopics().contains(topicTagData);
            }

            @Override
            public void handle(MessageExt message, CompletableConsumeConcurrentlyContext context) {
                PublishableExceptionConsumer.this.handle(message, context);
            }
        });
        return this;
    }

    public PublishableExceptionConsumer shutdown() {
        return this;
    }

    void handle(final MessageExt msg, final CompletableConsumeConcurrentlyContext context) {
        PublishableExceptionMessage exceptionMessage = _jsonSerializer.deserialize(BitConverter.toString(msg.getBody()), PublishableExceptionMessage.class);
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

        RocketMQProcessContext processContext = new RocketMQProcessContext(msg, context);
        ProcessingPublishableExceptionMessage processingMessage = new ProcessingPublishableExceptionMessage(exception, processContext);
        _logger.info("ENode exception message received, messageId: {}, aggregateRootId: {}, aggregateRootType: {}", exceptionMessage.getUniqueId(), exceptionMessage.getAggregateRootId(), exceptionMessage.getAggregateRootTypeName());
        _publishableExceptionProcessor.process(processingMessage);
    }

    public RocketMQConsumer getConsumer() {
        return _consumer;
    }
}

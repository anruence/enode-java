package com.enode.rocketmq.client;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.common.logging.ENodeLogger;
import com.enode.rocketmq.TopicTagData;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RocketMQConsumer {
    private static final Logger _logger = ENodeLogger.getLog();
    private Consumer _consumer;
    private Set<IMQMessageHandler> _handlers;
    private Map<TopicTagData, IMQMessageHandler> _handlerDict;

    @Inject
    public RocketMQConsumer(Consumer consumer) {
        _consumer = consumer;
        _consumer.registerMessageListener(new MessageHandler());
        _handlers = new HashSet<>();
        _handlerDict = new HashMap<>();
    }

    public void registerMessageHandler(IMQMessageHandler handler) {
        _handlers.add(handler);
    }

    public void subscribe(String topic, String subExpression) {
        _consumer.subscribe(topic, subExpression);
    }

    public void start() {
        _consumer.start();
    }

    public void shutdown() {
        _consumer.shutdown();
    }


    class MessageHandler implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            MessageExt msg = msgs.get(0);
            String topic = msg.getTopic();
            String tag = msg.getTags();
            TopicTagData topicTagData = new TopicTagData(topic, tag);

            IMQMessageHandler IMQMessageHandler = _handlerDict.get(topicTagData);

            ConsumeConcurrentlyStatus status = ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            if (IMQMessageHandler == null) {
                List<IMQMessageHandler> handlers = _handlers.stream().filter(handler -> handler.isMatched(topicTagData)).collect(Collectors.toList());
                if (handlers.size() > 1) {
                    _logger.error("Duplicate consume handler with {topic:{},tags:{}}", msg.getTopic(), msg.getTags());
                    status = ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                IMQMessageHandler = handlers.get(0);
                _handlerDict.put(topicTagData, IMQMessageHandler);
            }

            if (IMQMessageHandler == null) {
                _logger.error("No consume handler found with {topic:{},tags:{}}", msg.getTopic(), msg.getTags());
                status = ConsumeConcurrentlyStatus.RECONSUME_LATER;
            } else {
                IMQMessageHandler.handle(msg, null);
            }
            return status;
        }
    }
}

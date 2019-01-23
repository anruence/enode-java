package com.enode.rocketmq;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.utilities.BitConverter;
import com.enode.rocketmq.client.Consumer;
import com.enode.rocketmq.client.IMQMessageHandler;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RocketMQConsumer implements IMQConsumer {
    private static final Logger _logger = ENodeLogger.getLog();
    private Consumer _consumer;
    private Set<IMQMessageHandler> _handlers;
    private Map<String, IMQMessageHandler> _handlerDict;

    @Inject
    public RocketMQConsumer(Consumer consumer) {
        _consumer = consumer;
        _consumer.registerMessageListener(new MessageHandler());
        _handlers = new HashSet<>();
        _handlerDict = new HashMap<>();
    }

    @Override
    public void registerMessageHandler(IMQMessageHandler handler) {
        _handlers.add(handler);
    }

    @Override
    public void subscribe(String topic, String subExpression) {
        _consumer.subscribe(topic, subExpression);
    }

    @Override
    public void start() {
        _consumer.start();
    }

    @Override
    public void shutdown() {
        _consumer.shutdown();
    }


    class MessageHandler implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            MessageExt msg = msgs.get(0);
            String topic = msg.getTopic();
            String tag = msg.getTags() != null ? msg.getTags() : "";
            String key = topic + tag;
            TopicData topicTagData = new TopicData(topic, tag);
            IMQMessageHandler messageHandler = _handlerDict.get(key);
            ConsumeConcurrentlyStatus status = ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            if (messageHandler == null) {
                List<IMQMessageHandler> handlers = _handlers.stream().filter(handler -> handler.isMatched(topicTagData)).collect(Collectors.toList());
                if (handlers.size() != 1) {
                    _logger.error("Duplicate consume handler with {topic:{},tags:{}}", msg.getTopic(), msg.getTags());
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                messageHandler = handlers.get(0);
                _handlerDict.put(key, messageHandler);
            }
            messageHandler.handle(BitConverter.toString(msg.getBody()), null);
            return status;
        }
    }
}

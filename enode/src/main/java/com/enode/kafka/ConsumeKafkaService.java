package com.enode.kafka;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.enode.rocketmq.CompletableConsumeConcurrentlyContext;
import com.enode.rocketmq.IMQConsumer;
import com.enode.rocketmq.TopicData;
import com.enode.rocketmq.client.IMQMessageHandler;
import com.enode.rocketmq.command.ConsumeStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class ConsumeKafkaService implements IMQConsumer {
    private static final Logger _logger = ClientLogger.getLog();
    private final IMessageListener messageListener;
    private final KafkaConsumer kafkaConsumer;
    private final BlockingQueue<Runnable> consumeRequestQueue;
    private final ThreadPoolExecutor consumeExecutor;
    private Set<IMQMessageHandler> _handlers;
    private Set<String> _topics;
    private Map<TopicData, IMQMessageHandler> _handlerDict;

    @Inject
    public ConsumeKafkaService(KafkaConsumer kafkaConsumer) {
        this.messageListener = new MessageListener();
        this.kafkaConsumer = kafkaConsumer;
        this.consumeRequestQueue = new LinkedBlockingQueue<Runnable>();
        this.consumeExecutor = new ThreadPoolExecutor(
                1, 4,
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.consumeRequestQueue,
                new ThreadFactoryImpl("ConsumeMessageThread_"));
        _handlers = new HashSet<>();
        _topics = new HashSet<>();
        _handlerDict = new HashMap<>();
    }

    @Override
    public void registerMessageHandler(IMQMessageHandler handler) {
        _handlers.add(handler);
    }

    @Override
    public void subscribe(String topic, String subExpression) {
        _topics.add(topic);
    }

    @Override
    public void start() {
        kafkaConsumer.subscribe(_topics);
        this.consumeExecutor.submit(new KafkaConsumerRunner<>());
    }

    @Override
    public void shutdown() {
        this.consumeExecutor.shutdown();
    }

    class KafkaConsumerRunner<K, V> implements Runnable {
        private final AtomicBoolean closed = new AtomicBoolean(false);

        @Override
        public void run() {
            try {
                while (!closed.get()) {
                    ConsumerRecords<K, V> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord record : records) {
                        messageListener.recvMessage(record, null);
                    }
                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                if (!closed.get()) {
                    throw e;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                kafkaConsumer.close();
            }
        }

        public void shutdown() {
            closed.set(true);
            kafkaConsumer.wakeup();
        }

    }

    class MessageListener implements IMessageListener {
        @Override
        public ConsumeStatus recvMessage(ConsumerRecord msg, CompletableConsumeConcurrentlyContext context) {
            try {

                String topic = msg.topic();
                String tag = "";
                TopicData topicTagData = new TopicData(topic, tag);
                IMQMessageHandler messageHandler = _handlerDict.get(topicTagData);
                if (messageHandler == null) {
                    List<IMQMessageHandler> handlers = _handlers.stream().filter(handler -> handler.isMatched(topicTagData)).collect(Collectors.toList());
                    if (handlers.size() != 1) {
                        _logger.error("Duplicate consume handler with {topic:{}}}", msg.topic());
                        return ConsumeStatus.RECONSUME_LATER;
                    }
                    messageHandler = handlers.get(0);
                    _handlerDict.put(topicTagData, messageHandler);
                }
                messageHandler.handle(msg.value().toString(), null);
                return ConsumeStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                return null;
            }
        }
    }
}

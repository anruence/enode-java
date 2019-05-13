package com.enode.samples.commandhandles;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.enode.ENodeBootstrap;
import com.enode.commanding.impl.DefaultCommandProcessor;
import com.enode.commanding.impl.DefaultProcessingCommandHandler;
import com.enode.eventing.impl.DefaultEventService;
import com.enode.queue.TopicData;
import com.enode.rocketmq.message.RocketMQApplicationMessagePublisher;
import com.enode.rocketmq.message.RocketMQCommandListener;
import com.enode.rocketmq.message.RocketMQDomainEventPublisher;
import com.enode.rocketmq.message.RocketMQPublishableExceptionPublisher;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.enode.samples.Constant.APPLICATION_TOPIC;
import static com.enode.samples.Constant.COMMAND_CONSUMER_GROUP;
import static com.enode.samples.Constant.COMMAND_TOPIC;
import static com.enode.samples.Constant.EVENT_PRODUCER_GROUP;
import static com.enode.samples.Constant.EVENT_TOPIC;
import static com.enode.samples.Constant.EXCEPTION_TOPIC;
import static com.enode.samples.Constant.NAMESRVADDR;

@Configuration
public class AppConfigCommandConsumer {

    /**
     * 命令处理器
     *
     * @return
     */
    @Bean
    public DefaultProcessingCommandHandler defaultProcessingCommandHandler() {
        return new DefaultProcessingCommandHandler();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DefaultEventService defaultEventService() {
        return new DefaultEventService();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer defaultMQPushConsumer(RocketMQCommandListener rocketMQCommandListener) {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(COMMAND_CONSUMER_GROUP);
        defaultMQPushConsumer.setNamesrvAddr(NAMESRVADDR);
        Map<String, String> topic = new HashMap<>();
        topic.put(COMMAND_TOPIC, "*");
        defaultMQPushConsumer.setSubscription(topic);
        defaultMQPushConsumer.setMessageListener(rocketMQCommandListener);
        return defaultMQPushConsumer;
    }

    @Bean
    public RocketMQCommandListener rocketMQCommandListener() {
        return new RocketMQCommandListener();
    }


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer eventProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(NAMESRVADDR);
        producer.setProducerGroup(EVENT_PRODUCER_GROUP);
        return producer;
    }

    @Bean
    public RocketMQDomainEventPublisher rocketMQDomainEventPublisher(DefaultMQProducer eventProducer) {
        RocketMQDomainEventPublisher domainEventPublisher = new RocketMQDomainEventPublisher();
        domainEventPublisher.setProducer(eventProducer);
        domainEventPublisher.setTopicData(new TopicData(EVENT_TOPIC, "*"));
        return domainEventPublisher;
    }

    /**
     * 应用消息生产者，复用生产者实例发送到不同topic中
     *
     * @param eventProducer
     * @return
     */
    @Bean
    public RocketMQApplicationMessagePublisher rocketMQApplicationMessagePublisher(DefaultMQProducer eventProducer) {
        RocketMQApplicationMessagePublisher applicationMessagePublisher = new RocketMQApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(eventProducer);
        applicationMessagePublisher.setTopicData(new TopicData(APPLICATION_TOPIC, "*"));
        return applicationMessagePublisher;
    }

    /**
     * 异常消息生产者，复用生产者实例发送到不同topic中
     *
     * @param eventProducer
     * @return
     */
    @Bean
    public RocketMQPublishableExceptionPublisher rocketMQPublishableExceptionPublisher(DefaultMQProducer eventProducer) {
        RocketMQPublishableExceptionPublisher exceptionPublisher = new RocketMQPublishableExceptionPublisher();
        exceptionPublisher.setProducer(eventProducer);
        exceptionPublisher.setTopicData(new TopicData(EXCEPTION_TOPIC, "*"));
        return exceptionPublisher;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DefaultCommandProcessor defaultCommandProcessor() {
        return new DefaultCommandProcessor();
    }

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("com.enode.samples"));
        return bootstrap;
    }

}

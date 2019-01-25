package com.enode.rocketmq.message.config;

import com.enode.ENode;
import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandService;
import com.enode.common.container.GenericTypeLiteral;
import com.enode.common.container.LifeStyle;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.infrastructure.IPublishableException;
import com.enode.queue.ITopicProvider;
import com.enode.queue.SendReplyService;
import com.enode.queue.TopicTagData;
import com.enode.queue.command.CommandResultProcessor;
import com.enode.rocketmq.client.Producer;
import com.enode.rocketmq.client.RocketMQFactory;
import com.enode.rocketmq.client.impl.NativeMQFactory;
import com.enode.rocketmq.client.ons.ONSFactory;
import com.enode.rocketmq.message.RocketMQApplicationMessageConsumer;
import com.enode.rocketmq.message.RocketMQApplicationMessagePublisher;
import com.enode.rocketmq.message.RocketMQCommandConsumer;
import com.enode.rocketmq.message.RocketMQCommandService;
import com.enode.rocketmq.message.RocketMQDomainEventConsumer;
import com.enode.rocketmq.message.RocketMQDomainEventPublisher;
import com.enode.rocketmq.message.RocketMQPublishableExceptionConsumer;
import com.enode.rocketmq.message.RocketMQPublishableExceptionPublisher;
import com.enode.rocketmq.message.SendRocketMQService;

import java.util.Collection;
import java.util.Properties;

public class RocketMQConfig {

    private ENode enode;

    /**
     * ENode Components
     */
    public ENode getEnode() {
        return enode;
    }

    public static final int COMMAND_SERVICE = 1;
    public static final int DOMAIN_EVENT_PUBLISHER = 2;
    public static final int APPLICATION_MESSAGE_PUBLISHER = 4;
    public static final int EXCEPTION_PUBLISHER = 8;
    public static final int COMMAND_CONSUMER = 16;
    public static final int DOMAIN_EVENT_CONSUMER = 32;
    public static final int APPLICATION_MESSAGE_CONSUMER = 64;
    public static final int EXCEPTION_CONSUMER = 128;
    public static final int PUBLISHERS = COMMAND_SERVICE | DOMAIN_EVENT_PUBLISHER | APPLICATION_MESSAGE_PUBLISHER | EXCEPTION_PUBLISHER;
    public static final int CONSUMERS = COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER | APPLICATION_MESSAGE_CONSUMER | EXCEPTION_CONSUMER;
    public static final int ALL_COMPONENTS = PUBLISHERS | CONSUMERS;

    private Properties producerProps;

    private Properties consumerProps;

    private int registerMQFlag;

    public RocketMQConfig(ENode enode) {
        this.enode = enode;
    }


    /**
     * use rocketmq as CommandBus, EventBus
     *
     * @param producerProps
     * @param consumerProps
     * @param registerMQFlag
     * @param listenPort
     * @return
     */
    public RocketMQConfig useOns(Properties producerProps, Properties consumerProps, int registerMQFlag, int listenPort) {
        return useRocketMQ(producerProps, consumerProps, registerMQFlag, listenPort, true);
    }

    public RocketMQConfig useNativeRocketMQ(Properties producerProps, Properties consumerProps, int registerMQFlag, int listenPort) {
        return useRocketMQ(producerProps, consumerProps, registerMQFlag, listenPort, false);
    }

    private RocketMQConfig useRocketMQ(Properties producerProps, Properties consumerProps, int registerMQFlag, int listenPort, boolean isons) {
        this.registerMQFlag = registerMQFlag;
        this.producerProps = producerProps;
        this.consumerProps = consumerProps;
        enode.register(SendRocketMQService.class);
        RocketMQFactory mqFactory = isons ? new ONSFactory() : new NativeMQFactory();

        enode.register(RocketMQFactory.class, null, () -> mqFactory, LifeStyle.Singleton);
        Producer producer = mqFactory.createProducer(producerProps);
        enode.registerInstance(Producer.class, producer);
        //Create MQConsumer and any register consumers(KafkaCommandConsumer、KafkaDomainEventConsumer、KafkaApplicationMessageConsumer、KafkaPublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            // KafkaCommandConsumer、KafkaDomainEventConsumer需要引用SendReplyService
            if (hasAnyComponents(registerMQFlag, COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER)) {
                enode.register(SendReplyService.class);
            }

            //KafkaCommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                enode.register(RocketMQCommandConsumer.class);
            }

            //KafkaDomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                enode.register(RocketMQDomainEventConsumer.class);
            }

            //KafkaApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                enode.register(RocketMQApplicationMessageConsumer.class);
            }

            //KafkaPublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                enode.register(RocketMQPublishableExceptionConsumer.class);
            }
        }

        //register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {

            //CommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                enode.register(CommandResultProcessor.class, null, () -> {
                    IJsonSerializer jsonSerializer = enode.resolve(IJsonSerializer.class);
                    return new CommandResultProcessor(listenPort, jsonSerializer);
                }, LifeStyle.Singleton);
                enode.register(ICommandService.class, RocketMQCommandService.class);
            }

            //DomainEventPublisher
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_PUBLISHER)) {
                enode.register(new GenericTypeLiteral<IMessagePublisher<DomainEventStreamMessage>>() {
                }, RocketMQDomainEventPublisher.class);
            }

            //ApplicationMessagePublisher
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                enode.register(new GenericTypeLiteral<IMessagePublisher<IApplicationMessage>>() {
                }, RocketMQApplicationMessagePublisher.class);
            }

            //PublishableExceptionPublisher
            if (hasComponent(registerMQFlag, EXCEPTION_PUBLISHER)) {
                enode.register(new GenericTypeLiteral<IMessagePublisher<IPublishableException>>() {
                }, RocketMQPublishableExceptionPublisher.class);
            }
        }

        return this;
    }


    public boolean hasComponent(int componentsFlag, int checkComponent) {
        return (componentsFlag & checkComponent) == checkComponent;
    }

    public boolean hasAnyComponents(int componentsFlag, int checkComponents) {
        return (componentsFlag & checkComponents) > 0;
    }

    public RocketMQConfig start() {
        enode.start();
        startMQComponents();
        return this;
    }

    public void shutdown() {
        enode.shutdown();
        stopMQComponents();
    }

    private void startMQComponents() {
        //Start MQConsumer and any register consumers(KafkaCommandConsumer、KafkaDomainEventConsumer、KafkaApplicationMessageConsumer、KafkaPublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            //KafkaCommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                RocketMQCommandConsumer commandConsumer = enode.resolve(RocketMQCommandConsumer.class);
                commandConsumer.initializeQueue(consumerProps);
                //Command topics
                ITopicProvider<ICommand> commandTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<ICommand>>() {
                });
                Collection<TopicTagData> topicTagDatas = commandTopicProvider.getAllSubscribeTopics();
                topicTagDatas.forEach(t -> commandConsumer.subscribe(t.getTopic(), t.getTag()));
                commandConsumer.start();
            }

            //KafkaDomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                RocketMQDomainEventConsumer domainEventConsumer = enode.resolve(RocketMQDomainEventConsumer.class);
                domainEventConsumer.initializeQueue(consumerProps);
                //Domain event topics
                ITopicProvider<IDomainEvent> domainEventTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<IDomainEvent>>() {
                });
                Collection<TopicTagData> topicTagDatas = domainEventTopicProvider.getAllSubscribeTopics();
                topicTagDatas.forEach(t -> domainEventConsumer.subscribe(t.getTopic(), t.getTag()));
                domainEventConsumer.start();
            }

            //KafkaApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                RocketMQApplicationMessageConsumer applicationMessageConsumer = enode.resolve(RocketMQApplicationMessageConsumer.class);
                applicationMessageConsumer.initializeQueue(consumerProps);
                //Application message topics
                ITopicProvider<IApplicationMessage> applicationMessageTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<IApplicationMessage>>() {
                });
                Collection<TopicTagData> topicTagDatas = applicationMessageTopicProvider.getAllSubscribeTopics();
                topicTagDatas.forEach(t -> applicationMessageConsumer.subscribe(t.getTopic(), t.getTag()));
                applicationMessageConsumer.start();
            }

            //KafkaPublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                RocketMQPublishableExceptionConsumer publishableExceptionConsumer = enode.resolve(RocketMQPublishableExceptionConsumer.class);
                publishableExceptionConsumer.initializeQueue(consumerProps);
                //Exception topics
                ITopicProvider<IPublishableException> exceptionTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<IPublishableException>>() {
                });
                Collection<TopicTagData> topicTagDatas = exceptionTopicProvider.getAllSubscribeTopics();
                topicTagDatas.forEach(t -> publishableExceptionConsumer.subscribe(t.getTopic(), t.getTag()));
                publishableExceptionConsumer.start();
            }
        }

        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {
            //KafkaCommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                RocketMQCommandService commandService = enode.resolve(RocketMQCommandService.class);
                commandService.start();
            }

            //KafkaDomainEventPublisher
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_PUBLISHER)) {
                RocketMQDomainEventPublisher domainEventPublisher = enode.resolve(RocketMQDomainEventPublisher.class);
                domainEventPublisher.start();
            }

            //KafkaApplicationMessagePublisher
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                RocketMQApplicationMessagePublisher applicationMessagePublisher = enode.resolve(RocketMQApplicationMessagePublisher.class);
                applicationMessagePublisher.start();
            }

            //KafkaPublishableExceptionPublisher
            if (hasComponent(registerMQFlag, EXCEPTION_PUBLISHER)) {
                RocketMQPublishableExceptionPublisher exceptionPublisher = enode.resolve(RocketMQPublishableExceptionPublisher.class);
                exceptionPublisher.start();
            }
        }


    }

    private void stopMQComponents() {
        //Shutdown MQConsumer and any register consumers(KafkaCommandConsumer、KafkaDomainEventConsumer、KafkaApplicationMessageConsumer、KafkaPublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            //KafkaCommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                RocketMQCommandConsumer commandConsumer = enode.resolve(RocketMQCommandConsumer.class);
                commandConsumer.shutdown();
            }

            //KafkaDomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                RocketMQDomainEventConsumer domainEventConsumer = enode.resolve(RocketMQDomainEventConsumer.class);
                domainEventConsumer.shutdown();
            }

            //KafkaApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                RocketMQApplicationMessageConsumer applicationMessageConsumer = enode.resolve(RocketMQApplicationMessageConsumer.class);
                applicationMessageConsumer.shutdown();
            }

            //KafkaPublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                RocketMQPublishableExceptionConsumer publishableExceptionConsumer = enode.resolve(RocketMQPublishableExceptionConsumer.class);
                publishableExceptionConsumer.shutdown();
            }
        }

        // Shutdown MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {
            //KafkaCommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                RocketMQCommandService commandService = enode.resolve(RocketMQCommandService.class);
                commandService.shutdown();
            }

            //KafkaDomainEventPublisher
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_PUBLISHER)) {
                RocketMQDomainEventPublisher domainEventPublisher = enode.resolve(RocketMQDomainEventPublisher.class);
                domainEventPublisher.shutdown();
            }

            //KafkaApplicationMessagePublisher
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                RocketMQApplicationMessagePublisher applicationMessagePublisher = enode.resolve(RocketMQApplicationMessagePublisher.class);
                applicationMessagePublisher.shutdown();
            }

            //KafkaPublishableExceptionPublisher
            if (hasComponent(registerMQFlag, EXCEPTION_PUBLISHER)) {
                RocketMQPublishableExceptionPublisher exceptionPublisher = enode.resolve(RocketMQPublishableExceptionPublisher.class);
                exceptionPublisher.shutdown();
            }
        }
    }
}

package com.enode.kafka.config;

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
import com.enode.kafka.KafkaApplicationMessageConsumer;
import com.enode.kafka.KafkaApplicationMessagePublisher;
import com.enode.kafka.KafkaCommandConsumer;
import com.enode.kafka.KafkaCommandService;
import com.enode.kafka.KafkaDomainEventConsumer;
import com.enode.kafka.KafkaDomainEventPublisher;
import com.enode.kafka.KafkaPublishableExceptionConsumer;
import com.enode.kafka.KafkaPublishableExceptionPublisher;
import com.enode.kafka.SendMessageService;
import com.enode.queue.ITopicProvider;
import com.enode.queue.SendReplyService;
import com.enode.queue.TopicTagData;
import com.enode.queue.command.CommandResultProcessor;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static com.enode.ENode.*;

public class KafkaConfig {

    private ENode enode;
    private Properties producerProps;
    private Properties consumerProps;
    private int registerMQFlag;

    public KafkaConfig(ENode enode) {
        this.enode = enode;
    }

    /**
     * ENode Components
     */
    public ENode getEnode() {
        return enode;
    }

    /**
     * use kafka as CommandBus, EventBus
     *
     * @param producerProps
     * @param consumerProps
     * @param registerMQFlag
     * @param listenPort
     * @return
     */
    public KafkaConfig useKafka(Properties producerProps, Properties consumerProps, int registerMQFlag, int listenPort) {
        this.registerMQFlag = registerMQFlag;
        this.producerProps = producerProps;
        this.consumerProps = consumerProps;
        enode.register(SendMessageService.class);
        //Create MQConsumer and any register consumers(KafkaCommandConsumer、KafkaDomainEventConsumer、KafkaApplicationMessageConsumer、KafkaPublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            // KafkaCommandConsumer、KafkaDomainEventConsumer需要引用SendReplyService
            if (hasAnyComponents(registerMQFlag, COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER)) {
                enode.register(SendReplyService.class);
            }

            //KafkaCommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                enode.register(KafkaCommandConsumer.class);
            }

            //KafkaDomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                enode.register(KafkaDomainEventConsumer.class);
            }

            //KafkaApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                enode.register(KafkaApplicationMessageConsumer.class);
            }

            //KafkaPublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                enode.register(KafkaPublishableExceptionConsumer.class);
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
                enode.register(ICommandService.class, KafkaCommandService.class);
            }

            //DomainEventPublisher
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_PUBLISHER)) {
                enode.register(new GenericTypeLiteral<IMessagePublisher<DomainEventStreamMessage>>() {
                }, KafkaDomainEventPublisher.class);
            }

            //ApplicationMessagePublisher
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                enode.register(new GenericTypeLiteral<IMessagePublisher<IApplicationMessage>>() {
                }, KafkaApplicationMessagePublisher.class);
            }

            //PublishableExceptionPublisher
            if (hasComponent(registerMQFlag, EXCEPTION_PUBLISHER)) {
                enode.register(new GenericTypeLiteral<IMessagePublisher<IPublishableException>>() {
                }, KafkaPublishableExceptionPublisher.class);
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

    public KafkaConfig start() {
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
                KafkaCommandConsumer commandConsumer = enode.resolve(KafkaCommandConsumer.class);
                commandConsumer.initializeQueue(consumerProps);
                //Command topics
                ITopicProvider<ICommand> commandTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<ICommand>>() {
                });
                Collection<TopicTagData> topicTagDatas = commandTopicProvider.getAllSubscribeTopics();
                List<String> topics = Lists.newArrayList();
                topicTagDatas.forEach(topicTagData -> topics.add(topicTagData.getTopic()));
                commandConsumer.subscribe(topics);
                commandConsumer.start();
            }

            //KafkaDomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                KafkaDomainEventConsumer domainEventConsumer = enode.resolve(KafkaDomainEventConsumer.class);
                domainEventConsumer.initializeQueue(consumerProps);
                //Domain event topics
                ITopicProvider<IDomainEvent> domainEventTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<IDomainEvent>>() {
                });
                Collection<TopicTagData> topicTagDatas = domainEventTopicProvider.getAllSubscribeTopics();
                List<String> topics = Lists.newArrayList();
                topicTagDatas.forEach(topicTagData -> topics.add(topicTagData.getTopic()));
                domainEventConsumer.subscribe(topics);
                domainEventConsumer.start();
            }

            //KafkaApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                KafkaApplicationMessageConsumer applicationMessageConsumer = enode.resolve(KafkaApplicationMessageConsumer.class);
                applicationMessageConsumer.initializeQueue(consumerProps);
                //Application message topics
                ITopicProvider<IApplicationMessage> applicationMessageTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<IApplicationMessage>>() {
                });
                Collection<TopicTagData> topicTagDatas = applicationMessageTopicProvider.getAllSubscribeTopics();
                List<String> topics = Lists.newArrayList();
                topicTagDatas.forEach(topicTagData -> topics.add(topicTagData.getTopic()));
                applicationMessageConsumer.subscribe(topics);
                applicationMessageConsumer.start();
            }

            //KafkaPublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                KafkaPublishableExceptionConsumer publishableExceptionConsumer = enode.resolve(KafkaPublishableExceptionConsumer.class);
                publishableExceptionConsumer.initializeQueue(consumerProps);
                //Exception topics
                ITopicProvider<IPublishableException> exceptionTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<IPublishableException>>() {
                });
                Collection<TopicTagData> topicTagDatas = exceptionTopicProvider.getAllSubscribeTopics();
                List<String> topics = Lists.newArrayList();
                topicTagDatas.forEach(topicTagData -> topics.add(topicTagData.getTopic()));
                publishableExceptionConsumer.subscribe(topics);
                publishableExceptionConsumer.start();
            }
        }

        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {

            //KafkaCommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                KafkaCommandService commandService = enode.resolve(KafkaCommandService.class);
                commandService.initializeQueue(producerProps);
                commandService.start();
            }

            //KafkaDomainEventPublisher
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_PUBLISHER)) {
                KafkaDomainEventPublisher domainEventPublisher = enode.resolve(KafkaDomainEventPublisher.class);
                domainEventPublisher.initializeQueue(producerProps);
                domainEventPublisher.start();
            }

            //KafkaApplicationMessagePublisher
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                KafkaApplicationMessagePublisher applicationMessagePublisher = enode.resolve(KafkaApplicationMessagePublisher.class);
                applicationMessagePublisher.initializeQueue(producerProps);
                applicationMessagePublisher.start();
            }

            //KafkaPublishableExceptionPublisher
            if (hasComponent(registerMQFlag, EXCEPTION_PUBLISHER)) {
                KafkaPublishableExceptionPublisher exceptionPublisher = enode.resolve(KafkaPublishableExceptionPublisher.class);
                exceptionPublisher.initializeQueue(producerProps);
                exceptionPublisher.start();
            }
        }


    }

    private void stopMQComponents() {
        //Shutdown MQConsumer and any register consumers(KafkaCommandConsumer、KafkaDomainEventConsumer、KafkaApplicationMessageConsumer、KafkaPublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            //KafkaCommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                KafkaCommandConsumer commandConsumer = enode.resolve(KafkaCommandConsumer.class);
                commandConsumer.shutdown();
            }

            //KafkaDomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                KafkaDomainEventConsumer domainEventConsumer = enode.resolve(KafkaDomainEventConsumer.class);
                domainEventConsumer.shutdown();
            }

            //KafkaApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                KafkaApplicationMessageConsumer applicationMessageConsumer = enode.resolve(KafkaApplicationMessageConsumer.class);
                applicationMessageConsumer.shutdown();
            }

            //KafkaPublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                KafkaPublishableExceptionConsumer publishableExceptionConsumer = enode.resolve(KafkaPublishableExceptionConsumer.class);
                publishableExceptionConsumer.shutdown();
            }
        }

        // Shutdown MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {
            //KafkaCommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                KafkaCommandService commandService = enode.resolve(KafkaCommandService.class);
                commandService.shutdown();
            }

            //KafkaDomainEventPublisher
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_PUBLISHER)) {
                KafkaDomainEventPublisher domainEventPublisher = enode.resolve(KafkaDomainEventPublisher.class);
                domainEventPublisher.shutdown();
            }

            //KafkaApplicationMessagePublisher
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                KafkaApplicationMessagePublisher applicationMessagePublisher = enode.resolve(KafkaApplicationMessagePublisher.class);
                applicationMessagePublisher.shutdown();
            }

            //KafkaPublishableExceptionPublisher
            if (hasComponent(registerMQFlag, EXCEPTION_PUBLISHER)) {
                KafkaPublishableExceptionPublisher exceptionPublisher = enode.resolve(KafkaPublishableExceptionPublisher.class);
                exceptionPublisher.shutdown();
            }
        }
    }
}

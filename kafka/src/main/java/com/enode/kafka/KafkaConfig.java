package com.enode.kafka;

import com.enode.ENode;
import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandService;
import com.enode.common.container.GenericTypeLiteral;
import com.enode.common.container.LifeStyle;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.infrastructure.IPublishableException;
import com.enode.queue.IMQConsumer;
import com.enode.queue.IMQProducer;
import com.enode.queue.ITopicProvider;
import com.enode.queue.SendReplyService;
import com.enode.queue.TopicData;
import com.enode.queue.applicationmessage.ApplicationMessageConsumer;
import com.enode.queue.applicationmessage.ApplicationMessagePublisher;
import com.enode.queue.command.CommandConsumer;
import com.enode.queue.command.CommandResultProcessor;
import com.enode.queue.command.CommandService;
import com.enode.queue.domainevent.DomainEventConsumer;
import com.enode.queue.domainevent.DomainEventPublisher;
import com.enode.queue.publishableexceptions.PublishableExceptionConsumer;
import com.enode.queue.publishableexceptions.PublishableExceptionPublisher;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class KafkaConfig {

    private ENode eNode;

    public ENode getEnode() {
        return eNode;
    }

    //ENode Components
    public static final int COMMAND_SERVICE = 1;
    public static final int DOMAIN_EVENT_PUBLISHER = 2;
    public static final int APPLICATION_MESSAGE_PUBLISHER = 4;
    public static final int EXCEPTION_PUBLISHER = 8;
    public static final int COMMAND_CONSUMER = 16;
    public static final int DOMAIN_EVENT_CONSUMER = 32;
    public static final int APPLICATION_MESSAGE_CONSUMER = 64;
    public static final int EXCEPTION_CONSUMER = 128;
    //Default Composite Components
    public static final int PUBLISHERS = COMMAND_SERVICE | DOMAIN_EVENT_PUBLISHER | APPLICATION_MESSAGE_PUBLISHER | EXCEPTION_PUBLISHER;
    public static final int CONSUMERS = COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER | APPLICATION_MESSAGE_CONSUMER | EXCEPTION_CONSUMER;
    public static final int ALL_COMPONENTS = PUBLISHERS | CONSUMERS;

    private static final Logger logger = ENodeLogger.getLog();
    //加载AbstractDenormalizer
    private static final String[] ENODE_PACKAGE_SCAN = new String[]{"com.enode.domain", "com.enode.message", "com.enode.infrastructure.impl"};

    private int registerMQFlag;

    public KafkaConfig(ENode eNode) {
        this.eNode = eNode;
    }


    public KafkaConfig useKafka(Properties producerSetting, Properties consumerSetting, int registerMQFlag, int listenPort) {
        this.registerMQFlag = registerMQFlag;

        //Create MQConsumer and any register consumers(CommandConsumer、DomainEventConsumer、ApplicationMessageConsumer、PublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            KafkaConsumer kafkaConsumer = new KafkaConsumer(consumerSetting);
            eNode.register(KafkaConsumer.class, null, () -> kafkaConsumer, LifeStyle.Singleton);
            eNode.register(IMQConsumer.class, ConsumeKafkaService.class);


            // CommandConsumer、DomainEventConsumer需要引用SendReplyService
            if (hasAnyComponents(registerMQFlag, COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER)) {
                eNode.register(SendReplyService.class);
            }

            //CommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                eNode.register(CommandConsumer.class);
            }

            //DomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                eNode.register(DomainEventConsumer.class);
            }

            //ApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                eNode.register(ApplicationMessageConsumer.class);
            }

            //PublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                eNode.register(PublishableExceptionConsumer.class);
            }
        }

        //Create MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {
            //Create MQProducer
            KafkaProducer kafkaProducer = new KafkaProducer(producerSetting);
            eNode.register(KafkaProducer.class, null, () -> kafkaProducer, LifeStyle.Singleton);
            eNode.register(IMQProducer.class, SendKafkaService.class);
            //CommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                eNode.register(CommandResultProcessor.class, null, () -> {
                    IJsonSerializer jsonSerializer = eNode.resolve(IJsonSerializer.class);
                    return new CommandResultProcessor(listenPort, jsonSerializer);
                }, LifeStyle.Singleton);
                eNode.register(ICommandService.class, CommandService.class);
            }

            //DomainEventPublisher
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_PUBLISHER)) {
                eNode.register(new GenericTypeLiteral<IMessagePublisher<DomainEventStreamMessage>>() {
                }, DomainEventPublisher.class);
            }

            //ApplicationMessagePublisher
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                eNode.register(new GenericTypeLiteral<IMessagePublisher<IApplicationMessage>>() {
                }, ApplicationMessagePublisher.class);
            }

            //PublishableExceptionPublisher
            if (hasComponent(registerMQFlag, EXCEPTION_PUBLISHER)) {
                eNode.register(new GenericTypeLiteral<IMessagePublisher<IPublishableException>>() {
                }, PublishableExceptionPublisher.class);
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
        eNode.start();
        startMQComponents();
        return this;
    }

    public void shutdown() {
        eNode.shutdown();
        stopMQComponents();
    }

    private void startMQComponents() {
        //Start MQConsumer and any register consumers(CommandConsumer、DomainEventConsumer、ApplicationMessageConsumer、PublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            //All topic
            Set<TopicData> topicTagDatas = new HashSet<>();

            IMQConsumer imqConsumer = eNode.resolve(IMQConsumer.class);
            //CommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                CommandConsumer commandConsumer = eNode.resolve(CommandConsumer.class);
                commandConsumer.start();

                //Command topics
                ITopicProvider<ICommand> commandTopicProvider = eNode.resolve(new GenericTypeLiteral<ITopicProvider<ICommand>>() {
                });
                topicTagDatas.addAll(commandTopicProvider.getAllSubscribeTopics());
            }

            //DomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                DomainEventConsumer domainEventConsumer = eNode.resolve(DomainEventConsumer.class);
                domainEventConsumer.start();

                //Domain event topics
                ITopicProvider<IDomainEvent> domainEventTopicProvider = eNode.resolve(new GenericTypeLiteral<ITopicProvider<IDomainEvent>>() {
                });
                topicTagDatas.addAll(domainEventTopicProvider.getAllSubscribeTopics());
            }

            //ApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                ApplicationMessageConsumer applicationMessageConsumer = eNode.resolve(ApplicationMessageConsumer.class);
                applicationMessageConsumer.start();

                //Application message topics
                ITopicProvider<IApplicationMessage> applicationMessageTopicProvider = eNode.resolve(new GenericTypeLiteral<ITopicProvider<IApplicationMessage>>() {
                });
                if (applicationMessageTopicProvider != null) {
                    topicTagDatas.addAll(applicationMessageTopicProvider.getAllSubscribeTopics());
                }
            }

            //PublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                PublishableExceptionConsumer publishableExceptionConsumer = eNode.resolve(PublishableExceptionConsumer.class);
                publishableExceptionConsumer.start();

                //Exception topics
                ITopicProvider<IPublishableException> exceptionTopicProvider = eNode.resolve(new GenericTypeLiteral<ITopicProvider<IPublishableException>>() {
                });
                if (exceptionTopicProvider != null) {
                    topicTagDatas.addAll(exceptionTopicProvider.getAllSubscribeTopics());
                }
            }

            topicTagDatas.stream().collect(Collectors.groupingBy(TopicData::getTopic)).forEach((topic, tags) -> {
                String tagsJoin = tags.stream().map(TopicData::getTag).collect(Collectors.joining("||"));
                imqConsumer.subscribe(topic, tagsJoin);
            });

            imqConsumer.start();
        }

        //Start MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {
            //Start MQProducer
            IMQProducer producer = eNode.resolve(IMQProducer.class);
            producer.start();

            //CommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                ICommandService commandService = eNode.resolve(ICommandService.class);
                if (commandService instanceof CommandService) {
                    ((CommandService) commandService).start();
                }
            }
        }
    }

    private void stopMQComponents() {
        //Shutdown MQConsumer and any register consumers(CommandConsumer、DomainEventConsumer、ApplicationMessageConsumer、PublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            //CommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                CommandConsumer commandConsumer = eNode.resolve(CommandConsumer.class);
                commandConsumer.shutdown();
            }

            //DomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                DomainEventConsumer domainEventConsumer = eNode.resolve(DomainEventConsumer.class);
                domainEventConsumer.shutdown();
            }

            //ApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                ApplicationMessageConsumer applicationMessageConsumer = eNode.resolve(ApplicationMessageConsumer.class);
                applicationMessageConsumer.shutdown();
            }

            //PublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                PublishableExceptionConsumer publishableExceptionConsumer = eNode.resolve(PublishableExceptionConsumer.class);
                publishableExceptionConsumer.shutdown();
            }

            IMQConsumer consumer = eNode.resolve(IMQConsumer.class);
            consumer.shutdown();
        }

        //Shutdown MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {
            //Stop MQProducer
            IMQProducer producer = eNode.resolve(IMQProducer.class);
            producer.shutdown();

            //CommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                CommandService commandService = eNode.resolve(CommandService.class);
                commandService.shutdown();
            }
        }
    }
}

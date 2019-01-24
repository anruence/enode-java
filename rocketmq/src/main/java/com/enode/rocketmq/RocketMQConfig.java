package com.enode.rocketmq;

import com.enode.ENode;
import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandService;
import com.enode.common.container.GenericTypeLiteral;
import com.enode.common.container.LifeStyle;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.configurations.ConfigurationSetting;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.infrastructure.IPublishableException;
import com.enode.jmx.ENodeJMXAgent;
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
import com.enode.rocketmq.client.Consumer;
import com.enode.rocketmq.client.Producer;
import com.enode.rocketmq.client.RocketMQFactory;
import com.enode.rocketmq.client.impl.NativeMQFactory;
import com.enode.rocketmq.client.ons.ONSFactory;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class RocketMQConfig extends ENode {

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
    public static final int TYPE_ONS = 0;
    public static final int TYPE_ROCKETMQ = 1;
    public static final int TYPE_KAFKA = 2;
    private static final Logger logger = ENodeLogger.getLog();
    //加载AbstractDenormalizer
    private int registerMQFlag;

    public RocketMQConfig(ConfigurationSetting setting, String... packages) {
        super(setting, packages);
    }

    public RocketMQConfig useONS(Properties producerSetting, Properties consumerSetting, int registerMQFlag, int listenPort) {
        return useMQ(producerSetting, consumerSetting, registerMQFlag, listenPort, TYPE_ONS);
    }

    public RocketMQConfig useNativeRocketMQ(Properties producerSetting, Properties consumerSetting, int listenPort, int registerMQFlag) {
        return useMQ(producerSetting, consumerSetting, registerMQFlag, listenPort, TYPE_ROCKETMQ);
    }

    private RocketMQConfig useMQ(Properties producerSetting, Properties consumerSetting, int registerMQFlag, int listenPort, int mqType) {
        this.registerMQFlag = registerMQFlag;
        RocketMQFactory mqFactory = null;
        if (mqType == TYPE_ONS) {
            mqFactory = new ONSFactory();
        } else if (mqType == TYPE_ROCKETMQ) {
            mqFactory = new NativeMQFactory();
        }
        //Create MQConsumer and any register consumers(CommandConsumer、DomainEventConsumer、ApplicationMessageConsumer、PublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {


            Consumer consumer = mqFactory.createPushConsumer(consumerSetting);
            registerInstance(Consumer.class, consumer);
            register(IMQConsumer.class, RocketMQConsumer.class);

            // CommandConsumer、DomainEventConsumer需要引用SendReplyService
            if (hasAnyComponents(registerMQFlag, COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER)) {
                register(SendReplyService.class);
            }

            //CommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                register(CommandConsumer.class);
            }

            //DomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                register(DomainEventConsumer.class);
            }

            //ApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                register(ApplicationMessageConsumer.class);
            }

            //PublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                register(PublishableExceptionConsumer.class);
            }
        }

        //Create MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {
            //Create MQProducer

            Producer producer = mqFactory.createProducer(producerSetting);
            registerInstance(Producer.class, producer);
            register(IMQProducer.class, SendRocketMQService.class);

            //CommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                register(CommandResultProcessor.class, null, () -> {
                    IJsonSerializer jsonSerializer = resolve(IJsonSerializer.class);
                    return new CommandResultProcessor(listenPort, jsonSerializer);
                }, LifeStyle.Singleton);
                register(ICommandService.class, CommandService.class);
            }

            //DomainEventPublisher
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_PUBLISHER)) {
                register(new GenericTypeLiteral<IMessagePublisher<DomainEventStreamMessage>>() {
                }, DomainEventPublisher.class);
            }

            //ApplicationMessagePublisher
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                register(new GenericTypeLiteral<IMessagePublisher<IApplicationMessage>>() {
                }, ApplicationMessagePublisher.class);
            }

            //PublishableExceptionPublisher
            if (hasComponent(registerMQFlag, EXCEPTION_PUBLISHER)) {
                register(new GenericTypeLiteral<IMessagePublisher<IPublishableException>>() {
                }, PublishableExceptionPublisher.class);
            }
        }

        return this;
    }

    private void startMQComponents() {
        //Start MQConsumer and any register consumers(CommandConsumer、DomainEventConsumer、ApplicationMessageConsumer、PublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            //All topic
            Set<TopicData> topicTagDatas = new HashSet<>();

            IMQConsumer imqConsumer = resolve(IMQConsumer.class);
            //CommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                CommandConsumer commandConsumer = resolve(CommandConsumer.class);
                commandConsumer.start();

                //Command topics
                ITopicProvider<ICommand> commandTopicProvider = resolve(new GenericTypeLiteral<ITopicProvider<ICommand>>() {
                });
                topicTagDatas.addAll(commandTopicProvider.getAllSubscribeTopics());
            }

            //DomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                DomainEventConsumer domainEventConsumer = resolve(DomainEventConsumer.class);
                domainEventConsumer.start();

                //Domain event topics
                ITopicProvider<IDomainEvent> domainEventTopicProvider = resolve(new GenericTypeLiteral<ITopicProvider<IDomainEvent>>() {
                });
                topicTagDatas.addAll(domainEventTopicProvider.getAllSubscribeTopics());
            }

            //ApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                ApplicationMessageConsumer applicationMessageConsumer = resolve(ApplicationMessageConsumer.class);
                applicationMessageConsumer.start();

                //Application message topics
                ITopicProvider<IApplicationMessage> applicationMessageTopicProvider = resolve(new GenericTypeLiteral<ITopicProvider<IApplicationMessage>>() {
                });
                if (applicationMessageTopicProvider != null) {
                    topicTagDatas.addAll(applicationMessageTopicProvider.getAllSubscribeTopics());
                }
            }

            //PublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                PublishableExceptionConsumer publishableExceptionConsumer = resolve(PublishableExceptionConsumer.class);
                publishableExceptionConsumer.start();

                //Exception topics
                ITopicProvider<IPublishableException> exceptionTopicProvider = resolve(new GenericTypeLiteral<ITopicProvider<IPublishableException>>() {
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
            IMQProducer producer = resolve(IMQProducer.class);
            producer.start();

            //CommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                ICommandService commandService = resolve(ICommandService.class);
                if (commandService instanceof CommandService) {
                    ((CommandService) commandService).start();
                }
            }
        }
    }

    public boolean hasComponent(int componentsFlag, int checkComponent) {
        return (componentsFlag & checkComponent) == checkComponent;
    }

    public boolean hasAnyComponents(int componentsFlag, int checkComponents) {
        return (componentsFlag & checkComponents) > 0;
    }

    @Override
    public RocketMQConfig start() {
        startMQComponents();
        ENodeJMXAgent.startAgent();
        logger.info("ENode started.");
        return this;
    }

    @Override
    public void shutdown() {
        stopENodeComponents();
    }

    public void stopENodeComponents() {
        //Shutdown MQConsumer and any register consumers(CommandConsumer、DomainEventConsumer、ApplicationMessageConsumer、PublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            //CommandConsumer
            if (hasComponent(registerMQFlag, COMMAND_CONSUMER)) {
                CommandConsumer commandConsumer = resolve(CommandConsumer.class);
                commandConsumer.shutdown();
            }

            //DomainEventConsumer
            if (hasComponent(registerMQFlag, DOMAIN_EVENT_CONSUMER)) {
                DomainEventConsumer domainEventConsumer = resolve(DomainEventConsumer.class);
                domainEventConsumer.shutdown();
            }

            //ApplicationMessageConsumer
            if (hasComponent(registerMQFlag, APPLICATION_MESSAGE_CONSUMER)) {
                ApplicationMessageConsumer applicationMessageConsumer = resolve(ApplicationMessageConsumer.class);
                applicationMessageConsumer.shutdown();
            }

            //PublishableExceptionConsumer
            if (hasComponent(registerMQFlag, EXCEPTION_CONSUMER)) {
                PublishableExceptionConsumer publishableExceptionConsumer = resolve(PublishableExceptionConsumer.class);
                publishableExceptionConsumer.shutdown();
            }

            IMQConsumer consumer = resolve(IMQConsumer.class);
            consumer.shutdown();
        }

        //Shutdown MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerMQFlag, PUBLISHERS)) {
            //Stop MQProducer
            IMQProducer producer = resolve(IMQProducer.class);
            producer.shutdown();

            //CommandService
            if (hasComponent(registerMQFlag, COMMAND_SERVICE)) {
                CommandService commandService = resolve(CommandService.class);
                commandService.shutdown();
            }
        }
    }
}

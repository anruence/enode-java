package com.enode.rocketmq.message.config;

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
import com.enode.queue.ITopicProvider;
import com.enode.queue.SendReplyService;
import com.enode.queue.TopicTagData;
import com.enode.queue.command.CommandResultProcessor;
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
import org.slf4j.Logger;

import java.util.Collection;

import static com.enode.ENode.APPLICATION_MESSAGE_CONSUMER;
import static com.enode.ENode.APPLICATION_MESSAGE_PUBLISHER;
import static com.enode.ENode.COMMAND_CONSUMER;
import static com.enode.ENode.COMMAND_SERVICE;
import static com.enode.ENode.CONSUMERS;
import static com.enode.ENode.DOMAIN_EVENT_CONSUMER;
import static com.enode.ENode.DOMAIN_EVENT_PUBLISHER;
import static com.enode.ENode.EXCEPTION_CONSUMER;
import static com.enode.ENode.EXCEPTION_PUBLISHER;
import static com.enode.ENode.PUBLISHERS;

public class RocketMQConfig {

    private static Logger logger = ENodeLogger.getLog();

    private ENode enode;
    private MultiGroupProps multiGroupProps;

    public RocketMQConfig(ENode enode) {
        this.enode = enode;
    }

    /**
     * ENode Components
     */
    public ENode getEnode() {
        return enode;
    }

    /**
     * use rocketmq as CommandBus, EventBus
     *
     * @return
     */
    public RocketMQConfig useOns(MultiGroupProps multiGroupProps) {
        return useRocketMQ(multiGroupProps, true);
    }

    public RocketMQConfig useNativeRocketMQ(MultiGroupProps multiGroupProps) {
        return useRocketMQ(multiGroupProps, false);
    }

    private RocketMQConfig useRocketMQ(MultiGroupProps multiGroupProps, boolean isons) {
        this.multiGroupProps = multiGroupProps;
        int registerFlag = multiGroupProps.getRegisterFlag();
        enode.register(SendRocketMQService.class);
        RocketMQFactory mqFactory = isons ? new ONSFactory() : new NativeMQFactory();
        enode.registerInstance(RocketMQFactory.class, mqFactory);
        if (hasAnyComponents(registerFlag, CONSUMERS)) {
            //CommandConsumer、DomainEventConsumer需要引用SendReplyService
            if (hasAnyComponents(registerFlag, COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER)) {
                enode.register(SendReplyService.class);
            }
            //RocketMQCommandConsumer
            if (hasComponent(registerFlag, COMMAND_CONSUMER)) {
                enode.register(RocketMQCommandConsumer.class);
            }

            //RocketMQDomainEventConsumer
            if (hasComponent(registerFlag, DOMAIN_EVENT_CONSUMER)) {
                enode.register(RocketMQDomainEventConsumer.class);
            }

            //RocketMQApplicationMessageConsumer
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_CONSUMER)) {
                enode.register(RocketMQApplicationMessageConsumer.class);
            }

            //RocketMQPublishableExceptionConsumer
            if (hasComponent(registerFlag, EXCEPTION_CONSUMER)) {
                enode.register(RocketMQPublishableExceptionConsumer.class);
            }
        }

        //register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerFlag, PUBLISHERS)) {
            //CommandService
            if (hasComponent(registerFlag, COMMAND_SERVICE)) {
                enode.register(CommandResultProcessor.class, null, () -> {
                    IJsonSerializer jsonSerializer = enode.resolve(IJsonSerializer.class);
                    return new CommandResultProcessor(multiGroupProps.getListenPort(), jsonSerializer);
                }, LifeStyle.Singleton);
                enode.register(ICommandService.class, RocketMQCommandService.class);
            }

            //DomainEventPublisher
            if (hasComponent(registerFlag, DOMAIN_EVENT_PUBLISHER)) {
                enode.register(new GenericTypeLiteral<IMessagePublisher<DomainEventStreamMessage>>() {
                }, RocketMQDomainEventPublisher.class);
            }

            //ApplicationMessagePublisher
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                enode.register(new GenericTypeLiteral<IMessagePublisher<IApplicationMessage>>() {
                }, RocketMQApplicationMessagePublisher.class);
            }

            //PublishableExceptionPublisher
            if (hasComponent(registerFlag, EXCEPTION_PUBLISHER)) {
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
        //Start MQConsumer and any register consumers(RocketMQCommandConsumer、RocketMQDomainEventConsumer、RocketMQApplicationMessageConsumer、RocketMQPublishableExceptionConsumer)
        int registerFlag = multiGroupProps.getRegisterFlag();
        if (hasAnyComponents(registerFlag, CONSUMERS)) {
            //RocketMQCommandConsumer
            if (hasComponent(registerFlag, COMMAND_CONSUMER)) {
                RocketMQCommandConsumer commandConsumer = enode.resolve(RocketMQCommandConsumer.class);
                commandConsumer.initializeQueue(multiGroupProps.getCommandProps().getConsumerProps());
                //Command topics
                ITopicProvider<ICommand> commandTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<ICommand>>() {
                });
                Collection<TopicTagData> topicTagDatas = commandTopicProvider.getAllSubscribeTopics();
                topicTagDatas.forEach(t -> commandConsumer.subscribe(t.getTopic(), t.getTag()));
                commandConsumer.start();
            }

            //RocketMQDomainEventConsumer
            if (hasComponent(registerFlag, DOMAIN_EVENT_CONSUMER)) {
                RocketMQDomainEventConsumer domainEventConsumer = enode.resolve(RocketMQDomainEventConsumer.class);
                domainEventConsumer.initializeQueue(multiGroupProps.getEventProps().getConsumerProps());
                //Domain event topics
                ITopicProvider<IDomainEvent> domainEventTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<IDomainEvent>>() {
                });
                Collection<TopicTagData> topicTagDatas = domainEventTopicProvider.getAllSubscribeTopics();
                topicTagDatas.forEach(t -> domainEventConsumer.subscribe(t.getTopic(), t.getTag()));
                domainEventConsumer.start();
            }

            //RocketMQApplicationMessageConsumer
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_CONSUMER)) {
                RocketMQApplicationMessageConsumer applicationMessageConsumer = enode.resolve(RocketMQApplicationMessageConsumer.class);
                applicationMessageConsumer.initializeQueue(multiGroupProps.getApplicationProps().getConsumerProps());
                //Application message topics
                ITopicProvider<IApplicationMessage> applicationMessageTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<IApplicationMessage>>() {
                });
                Collection<TopicTagData> topicTagDatas = applicationMessageTopicProvider.getAllSubscribeTopics();
                topicTagDatas.forEach(t -> applicationMessageConsumer.subscribe(t.getTopic(), t.getTag()));
                applicationMessageConsumer.start();
            }

            //RocketMQPublishableExceptionConsumer
            if (hasComponent(registerFlag, EXCEPTION_CONSUMER)) {
                RocketMQPublishableExceptionConsumer publishableExceptionConsumer = enode.resolve(RocketMQPublishableExceptionConsumer.class);
                publishableExceptionConsumer.initializeQueue(multiGroupProps.getExceptionProps().getConsumerProps());
                //Exception topics
                ITopicProvider<IPublishableException> exceptionTopicProvider = enode.resolve(new GenericTypeLiteral<ITopicProvider<IPublishableException>>() {
                });
                Collection<TopicTagData> topicTagDatas = exceptionTopicProvider.getAllSubscribeTopics();
                topicTagDatas.forEach(t -> publishableExceptionConsumer.subscribe(t.getTopic(), t.getTag()));
                publishableExceptionConsumer.start();
            }
        }

        if (hasAnyComponents(registerFlag, PUBLISHERS)) {
            //RocketMQCommandService
            if (hasComponent(registerFlag, COMMAND_SERVICE)) {
                RocketMQCommandService commandService = enode.resolve(RocketMQCommandService.class);
                commandService.initializeQueue(multiGroupProps.getCommandProps().getProducerProps());
                commandService.start();
            }

            //RocketMQDomainEventPublisher
            if (hasComponent(registerFlag, DOMAIN_EVENT_PUBLISHER)) {
                RocketMQDomainEventPublisher domainEventPublisher = enode.resolve(RocketMQDomainEventPublisher.class);
                domainEventPublisher.initializeQueue(multiGroupProps.getEventProps().getProducerProps());
                domainEventPublisher.start();
            }

            //RocketMQApplicationMessagePublisher
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                RocketMQApplicationMessagePublisher applicationMessagePublisher = enode.resolve(RocketMQApplicationMessagePublisher.class);
                applicationMessagePublisher.initializeQueue(multiGroupProps.getApplicationProps().getProducerProps());
                applicationMessagePublisher.start();
            }

            //RocketMQPublishableExceptionPublisher
            if (hasComponent(registerFlag, EXCEPTION_PUBLISHER)) {
                RocketMQPublishableExceptionPublisher exceptionPublisher = enode.resolve(RocketMQPublishableExceptionPublisher.class);
                exceptionPublisher.initializeQueue(multiGroupProps.getExceptionProps().getProducerProps());
                exceptionPublisher.start();
            }
        }


    }

    private void stopMQComponents() {
        //Shutdown MQConsumer and any register consumers(RocketMQCommandConsumer、RocketMQDomainEventConsumer、RocketMQApplicationMessageConsumer、RocketMQPublishableExceptionConsumer)
        int registerFlag = multiGroupProps.getRegisterFlag();

        if (hasAnyComponents(registerFlag, CONSUMERS)) {
            //RocketMQCommandConsumer
            if (hasComponent(registerFlag, COMMAND_CONSUMER)) {
                RocketMQCommandConsumer commandConsumer = enode.resolve(RocketMQCommandConsumer.class);
                commandConsumer.shutdown();
            }

            //RocketMQDomainEventConsumer
            if (hasComponent(registerFlag, DOMAIN_EVENT_CONSUMER)) {
                RocketMQDomainEventConsumer domainEventConsumer = enode.resolve(RocketMQDomainEventConsumer.class);
                domainEventConsumer.shutdown();
            }

            //RocketMQApplicationMessageConsumer
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_CONSUMER)) {
                RocketMQApplicationMessageConsumer applicationMessageConsumer = enode.resolve(RocketMQApplicationMessageConsumer.class);
                applicationMessageConsumer.shutdown();
            }

            //RocketMQPublishableExceptionConsumer
            if (hasComponent(registerFlag, EXCEPTION_CONSUMER)) {
                RocketMQPublishableExceptionConsumer publishableExceptionConsumer = enode.resolve(RocketMQPublishableExceptionConsumer.class);
                publishableExceptionConsumer.shutdown();
            }
        }

        // Shutdown MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerFlag, PUBLISHERS)) {
            //RocketMQCommandService
            if (hasComponent(registerFlag, COMMAND_SERVICE)) {
                RocketMQCommandService commandService = enode.resolve(RocketMQCommandService.class);
                commandService.shutdown();
            }

            //RocketMQDomainEventPublisher
            if (hasComponent(registerFlag, DOMAIN_EVENT_PUBLISHER)) {
                RocketMQDomainEventPublisher domainEventPublisher = enode.resolve(RocketMQDomainEventPublisher.class);
                domainEventPublisher.shutdown();
            }

            //RocketMQApplicationMessagePublisher
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                RocketMQApplicationMessagePublisher applicationMessagePublisher = enode.resolve(RocketMQApplicationMessagePublisher.class);
                applicationMessagePublisher.shutdown();
            }

            //RocketMQPublishableExceptionPublisher
            if (hasComponent(registerFlag, EXCEPTION_PUBLISHER)) {
                RocketMQPublishableExceptionPublisher exceptionPublisher = enode.resolve(RocketMQPublishableExceptionPublisher.class);
                exceptionPublisher.shutdown();
            }
        }
    }
}

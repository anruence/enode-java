package com.enode.rocketmq.message.config;

import com.enode.rocketmq.message.RocketMQApplicationMessageConsumer;
import com.enode.rocketmq.message.RocketMQApplicationMessagePublisher;
import com.enode.rocketmq.message.RocketMQCommandConsumer;
import com.enode.rocketmq.message.RocketMQCommandService;
import com.enode.rocketmq.message.RocketMQDomainEventConsumer;
import com.enode.rocketmq.message.RocketMQDomainEventPublisher;
import com.enode.rocketmq.message.RocketMQPublishableExceptionConsumer;
import com.enode.rocketmq.message.RocketMQPublishableExceptionPublisher;
import org.springframework.beans.factory.annotation.Autowired;

public class RocketMQConfig {

    public int getRegisterFlag() {
        return registerFlag;
    }

    public void setRegisterFlag(int registerFlag) {
        this.registerFlag = registerFlag;
    }

    private int registerFlag;

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

    /**
     * use rocketmq as CommandBus, EventBus
     */
    @Autowired(required = false)
    private RocketMQCommandConsumer commandConsumer;

    @Autowired(required = false)
    private RocketMQDomainEventConsumer domainEventConsumer;

    @Autowired(required = false)
    private RocketMQApplicationMessageConsumer applicationMessageConsumer;

    @Autowired(required = false)
    private RocketMQPublishableExceptionConsumer publishableExceptionConsumer;

    @Autowired(required = false)
    private RocketMQCommandService commandService;

    @Autowired(required = false)
    private RocketMQDomainEventPublisher domainEventPublisher;

    @Autowired(required = false)
    private RocketMQApplicationMessagePublisher applicationMessagePublisher;

    @Autowired(required = false)
    private RocketMQPublishableExceptionPublisher exceptionPublisher;

    private boolean hasComponent(int componentsFlag, int checkComponent) {
        return (componentsFlag & checkComponent) == checkComponent;
    }

    private boolean hasAnyComponents(int componentsFlag, int checkComponents) {
        return (componentsFlag & checkComponents) > 0;
    }


    public RocketMQConfig start() {
        //Start MQConsumer and any register consumers(RocketMQCommandConsumer、RocketMQDomainEventConsumer、RocketMQApplicationMessageConsumer、RocketMQPublishableExceptionConsumer)
        if (hasAnyComponents(registerFlag, CONSUMERS)) {
            //RocketMQCommandConsumer
            if (hasComponent(registerFlag, COMMAND_CONSUMER)) {
                commandConsumer.start();
            }

            // RocketMQDomainEventConsumer
            if (hasComponent(registerFlag, DOMAIN_EVENT_CONSUMER)) {
                //Domain event topics
                domainEventConsumer.start();
            }

            // RocketMQApplicationMessageConsumer
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_CONSUMER)) {
                //Application message topics
                applicationMessageConsumer.start();
            }

            // RocketMQPublishableExceptionConsumer
            if (hasComponent(registerFlag, EXCEPTION_CONSUMER)) {
                publishableExceptionConsumer.start();
            }
        }

        if (hasAnyComponents(registerFlag, PUBLISHERS)) {
            //RocketMQCommandService
            if (hasComponent(registerFlag, COMMAND_SERVICE)) {
                commandService.start();
            }

            //RocketMQDomainEventPublisher
            if (hasComponent(registerFlag, DOMAIN_EVENT_PUBLISHER)) {
                domainEventPublisher.start();
            }

            //RocketMQApplicationMessagePublisher
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                applicationMessagePublisher.start();
            }

            //RocketMQPublishableExceptionPublisher
            if (hasComponent(registerFlag, EXCEPTION_PUBLISHER)) {
                exceptionPublisher.start();
            }
        }
        return this;
    }

    public RocketMQConfig shutdown() {
        //Shutdown MQConsumer and any register consumers(RocketMQCommandConsumer、RocketMQDomainEventConsumer、RocketMQApplicationMessageConsumer、RocketMQPublishableExceptionConsumer)

        if (hasAnyComponents(registerFlag, CONSUMERS)) {
            //RocketMQCommandConsumer
            if (hasComponent(registerFlag, COMMAND_CONSUMER)) {
                commandConsumer.shutdown();
            }

            //RocketMQDomainEventConsumer
            if (hasComponent(registerFlag, DOMAIN_EVENT_CONSUMER)) {
                domainEventConsumer.shutdown();
            }

            //RocketMQApplicationMessageConsumer
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_CONSUMER)) {
                applicationMessageConsumer.shutdown();
            }

            //RocketMQPublishableExceptionConsumer
            if (hasComponent(registerFlag, EXCEPTION_CONSUMER)) {
                publishableExceptionConsumer.shutdown();
            }
        }

        // Shutdown MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerFlag, PUBLISHERS)) {
            //RocketMQCommandService
            if (hasComponent(registerFlag, COMMAND_SERVICE)) {
                commandService.shutdown();
            }

            //RocketMQDomainEventPublisher
            if (hasComponent(registerFlag, DOMAIN_EVENT_PUBLISHER)) {
                domainEventPublisher.shutdown();
            }

            //RocketMQApplicationMessagePublisher
            if (hasComponent(registerFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                applicationMessagePublisher.shutdown();
            }

            //RocketMQPublishableExceptionPublisher
            if (hasComponent(registerFlag, EXCEPTION_PUBLISHER)) {
                exceptionPublisher.shutdown();
            }
        }
        return this;
    }

}

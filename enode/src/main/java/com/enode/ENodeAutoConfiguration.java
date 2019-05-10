package com.enode;

import com.enode.commanding.impl.CommandAsyncHandlerProxy;
import com.enode.commanding.impl.CommandHandlerProxy;
import com.enode.commanding.impl.DefaultCommandAsyncHandlerProvider;
import com.enode.commanding.impl.DefaultCommandHandlerProvider;
import com.enode.commanding.impl.DefaultCommandRoutingKeyProvider;
import com.enode.common.container.SpringObjectContainer;
import com.enode.common.extensions.ApplicationContextHelper;
import com.enode.common.io.IOHelper;
import com.enode.common.scheduling.ScheduleService;
import com.enode.common.thirdparty.gson.GsonJsonSerializer;
import com.enode.domain.impl.DefaultAggregateRepositoryProvider;
import com.enode.domain.impl.DefaultAggregateRootFactory;
import com.enode.domain.impl.DefaultAggregateRootInternalHandlerProvider;
import com.enode.domain.impl.DefaultAggregateSnapshotter;
import com.enode.domain.impl.DefaultMemoryCache;
import com.enode.domain.impl.DefaultRepository;
import com.enode.domain.impl.EventSourcingAggregateStorage;
import com.enode.eventing.impl.DefaultEventSerializer;
import com.enode.eventing.impl.InMemoryEventStore;
import com.enode.infrastructure.impl.DefaultApplicationMessageProcessor;
import com.enode.infrastructure.impl.DefaultDomainEventProcessor;
import com.enode.infrastructure.impl.DefaultMessageDispatcher;
import com.enode.infrastructure.impl.DefaultMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultProcessingMessageHandler;
import com.enode.infrastructure.impl.DefaultProcessingMessageScheduler;
import com.enode.infrastructure.impl.DefaultPublishableExceptionProcessor;
import com.enode.infrastructure.impl.DefaultThreeMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultTwoMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultTypeNameProvider;
import com.enode.infrastructure.impl.MessageHandlerProxy1;
import com.enode.infrastructure.impl.MessageHandlerProxy2;
import com.enode.infrastructure.impl.MessageHandlerProxy3;
import com.enode.infrastructure.impl.inmemory.InMemoryPublishedVersionStore;
import com.enode.queue.SendReplyService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ENodeAutoConfiguration {

    @Bean
    public GsonJsonSerializer jsonSerializer() {
        return new GsonJsonSerializer();
    }

    @Bean
    public ScheduleService scheduleService() {
        return new ScheduleService();
    }

    @Bean
    public IOHelper ioHelper() {
        return new IOHelper();
    }

    @Bean
    public ApplicationContextHelper applicationContextHelper() {
        return new ApplicationContextHelper();
    }

    @Bean
    public DefaultProcessingMessageScheduler defaultProcessingMessageScheduler() {
        return new DefaultProcessingMessageScheduler();
    }

    @Bean
    public DefaultTypeNameProvider defaultTypeNameProvider() {
        return new DefaultTypeNameProvider();
    }

    @Bean
    public DefaultProcessingMessageHandler defaultProcessingMessageHandler() {
        return new DefaultProcessingMessageHandler();
    }

    @Bean
    public DefaultPublishableExceptionProcessor defaultPublishableExceptionProcessor() {
        return new DefaultPublishableExceptionProcessor();
    }

    @Bean
    public DefaultApplicationMessageProcessor defaultApplicationMessageProcessor() {
        return new DefaultApplicationMessageProcessor();
    }

    @Bean
    public DefaultDomainEventProcessor defaultDomainEventProcessor() {
        return new DefaultDomainEventProcessor();
    }

    @Bean
    public SpringObjectContainer springObjectContainer() {
        return new SpringObjectContainer();
    }


    /**
     * 原型模式获取bean，每次新建代理执行
     *
     * @return
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CommandHandlerProxy commandHandlerProxy() {
        return new CommandHandlerProxy();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CommandAsyncHandlerProxy commandAsyncHandlerProxy() {
        return new CommandAsyncHandlerProxy();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MessageHandlerProxy1 messageHandlerProxy1() {
        return new MessageHandlerProxy1();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MessageHandlerProxy2 messageHandlerProxy2() {
        return new MessageHandlerProxy2();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MessageHandlerProxy3 messageHandlerProxy3() {
        return new MessageHandlerProxy3();
    }

    @Bean
    public DefaultEventSerializer defaultEventSerializer() {
        return new DefaultEventSerializer();
    }


    @Bean(initMethod = "start", destroyMethod = "stop")
    public SendReplyService sendReplyService() {
        return new SendReplyService();
    }

    @Bean
    public DefaultAggregateRootInternalHandlerProvider aggregateRootInternalHandlerProvider() {
        return new DefaultAggregateRootInternalHandlerProvider();
    }

    @Bean
    public DefaultMessageDispatcher defaultMessageDispatcher() {
        return new DefaultMessageDispatcher();
    }

    @Bean
    public DefaultRepository defaultRepository() {
        return new DefaultRepository();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DefaultMemoryCache defaultMemoryCache() {
        return new DefaultMemoryCache();
    }

    @Bean
    public DefaultCommandRoutingKeyProvider commandRoutingKeyProvider() {
        return new DefaultCommandRoutingKeyProvider();
    }

    @Bean
    public DefaultAggregateRepositoryProvider aggregateRepositoryProvider() {
        return new DefaultAggregateRepositoryProvider();
    }

    @Bean
    public DefaultThreeMessageHandlerProvider threeMessageHandlerProvider() {
        return new DefaultThreeMessageHandlerProvider();
    }

    @Bean
    public DefaultTwoMessageHandlerProvider twoMessageHandlerProvider() {
        return new DefaultTwoMessageHandlerProvider();
    }

    @Bean
    public DefaultMessageHandlerProvider messageHandlerProvider() {
        return new DefaultMessageHandlerProvider();
    }

    @Bean
    public DefaultCommandAsyncHandlerProvider commandAsyncHandlerProvider() {
        return new DefaultCommandAsyncHandlerProvider();
    }

    @Bean
    public DefaultCommandHandlerProvider commandHandlerProvider() {
        return new DefaultCommandHandlerProvider();
    }

    @Bean
    public DefaultAggregateRootFactory aggregateRootFactory() {
        return new DefaultAggregateRootFactory();
    }

    @Bean
    public DefaultAggregateSnapshotter aggregateSnapshotter() {
        return new DefaultAggregateSnapshotter();
    }

    @Bean
    public EventSourcingAggregateStorage eventSourcingAggregateStorage() {
        return new EventSourcingAggregateStorage();
    }

    @Bean
    public InMemoryEventStore eventStore() {
        return new InMemoryEventStore();
    }

    @Bean
    public InMemoryPublishedVersionStore publishedVersionStore() {
        return new InMemoryPublishedVersionStore();
    }
}


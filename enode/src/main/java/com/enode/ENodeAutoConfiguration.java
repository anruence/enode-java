package com.enode;

import com.enode.commanding.impl.DefaultCommandAsyncHandlerProvider;
import com.enode.commanding.impl.DefaultCommandHandlerProvider;
import com.enode.commanding.impl.DefaultCommandRoutingKeyProvider;
import com.enode.common.io.IOHelper;
import com.enode.common.scheduling.ScheduleService;
import com.enode.common.thirdparty.gson.GsonJsonSerializer;
import com.enode.domain.IAggregateRepositoryProvider;
import com.enode.domain.impl.DefaultAggregateRepositoryProvider;
import com.enode.domain.impl.DefaultAggregateRootFactory;
import com.enode.domain.impl.DefaultAggregateRootInternalHandlerProvider;
import com.enode.domain.impl.DefaultAggregateSnapshotter;
import com.enode.domain.impl.EventSourcingAggregateStorage;
import com.enode.eventing.impl.DefaultEventSerializer;
import com.enode.eventing.impl.InMemoryEventStore;
import com.enode.infrastructure.ITimeProvider;
import com.enode.infrastructure.impl.DefaultMessageDispatcher;
import com.enode.infrastructure.impl.DefaultMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultThreeMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultTimeProvider;
import com.enode.infrastructure.impl.DefaultTwoMessageHandlerProvider;
import com.enode.infrastructure.impl.inmemory.InMemoryPublishedVersionStore;
import com.enode.queue.SendReplyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public DefaultEventSerializer defaultEventSerializer() {
        return new DefaultEventSerializer();
    }

    @Bean
    public IOHelper ioHelper() {
        return new IOHelper();
    }

    @Bean
    public ITimeProvider timeProvider() {
        return new DefaultTimeProvider();
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
    public EventSourcingAggregateStorage eventSourcingAggregateStorage() {
        return new EventSourcingAggregateStorage();
    }

    @Bean
    public DefaultAggregateRootFactory aggregateRootFactory() {
        return new DefaultAggregateRootFactory();
    }

    @Bean
    public DefaultAggregateSnapshotter aggregateSnapshotter(IAggregateRepositoryProvider aggregateRepositoryProvider) {
        return new DefaultAggregateSnapshotter(aggregateRepositoryProvider);
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


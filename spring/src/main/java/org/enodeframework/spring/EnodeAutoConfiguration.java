package org.enodeframework.spring;

import com.google.common.collect.Maps;
import org.enodeframework.commanding.ICommandHandlerProvider;
import org.enodeframework.commanding.ICommandProcessor;
import org.enodeframework.commanding.IProcessingCommandHandler;
import org.enodeframework.commanding.impl.DefaultCommandHandlerProvider;
import org.enodeframework.commanding.impl.DefaultCommandProcessor;
import org.enodeframework.commanding.impl.DefaultProcessingCommandHandler;
import org.enodeframework.common.scheduling.IScheduleService;
import org.enodeframework.common.scheduling.DefaultScheduleService;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.common.serializing.DefaultSerializeService;
import org.enodeframework.domain.IAggregateRepositoryProvider;
import org.enodeframework.domain.IAggregateRootFactory;
import org.enodeframework.domain.IAggregateSnapshotter;
import org.enodeframework.domain.IAggregateStorage;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.domain.IMemoryCache;
import org.enodeframework.domain.IRepository;
import org.enodeframework.domain.impl.DefaultAggregateRepositoryProvider;
import org.enodeframework.domain.impl.DefaultAggregateRootFactory;
import org.enodeframework.domain.impl.DefaultAggregateRootInternalHandlerProvider;
import org.enodeframework.domain.impl.DefaultAggregateSnapshotter;
import org.enodeframework.domain.impl.DefaultMemoryCache;
import org.enodeframework.domain.impl.DefaultRepository;
import org.enodeframework.domain.impl.EventSourcingAggregateStorage;
import org.enodeframework.domain.impl.SnapshotOnlyAggregateStorage;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.IEventCommittingService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.eventing.IEventStore;
import org.enodeframework.eventing.IProcessingEventProcessor;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.enodeframework.eventing.impl.DefaultEventCommittingService;
import org.enodeframework.eventing.impl.DefaultEventSerializer;
import org.enodeframework.eventing.impl.DefaultProcessingEventProcessor;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.infrastructure.impl.DefaultTypeNameProvider;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessageDispatcher;
import org.enodeframework.messaging.IMessageHandlerProvider;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.messaging.IThreeMessageHandlerProvider;
import org.enodeframework.messaging.ITwoMessageHandlerProvider;
import org.enodeframework.messaging.impl.DefaultMessageDispatcher;
import org.enodeframework.messaging.impl.DefaultMessageHandlerProvider;
import org.enodeframework.messaging.impl.DefaultThreeMessageHandlerProvider;
import org.enodeframework.messaging.impl.DefaultTwoMessageHandlerProvider;
import org.enodeframework.queue.DefaultSendReplyService;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.ISendReplyService;
import org.enodeframework.queue.applicationmessage.DefaultApplicationMessageHandler;
import org.enodeframework.queue.applicationmessage.DefaultApplicationMessagePublisher;
import org.enodeframework.queue.command.DefaultCommandMessageHandler;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
import org.enodeframework.queue.command.DefaultCommandService;
import org.enodeframework.queue.command.ICommandResultProcessor;
import org.enodeframework.queue.domainevent.DefaultDomainEventMessageHandler;
import org.enodeframework.queue.domainevent.DefaultDomainEventPublisher;
import org.enodeframework.queue.publishableexceptions.DefaultPublishableExceptionMessageHandler;
import org.enodeframework.queue.publishableexceptions.DefaultPublishableExceptionPublisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author anruence@gmail.com
 */
public class EnodeAutoConfiguration {

    @Value("${spring.enode.mq.topic.command:}")
    private String commandTopic;

    @Value("${spring.enode.mq.topic.event:}")
    private String eventTopic;

    @Value("${spring.enode.mq.topic.application:}")
    private String applicationTopic;

    @Value("${spring.enode.mq.topic.exception:}")
    private String exceptionTopic;

    @Value("${spring.enode.mq.tag.command:*}")
    private String commandTag;

    @Value("${spring.enode.mq.tag.event:*}")
    private String eventTag;

    @Value("${spring.enode.mq.tag.application:*}")
    private String applicationTag;

    @Value("${spring.enode.mq.tag.exception:*}")
    private String exceptionTag;

    @Value("${spring.enode.server.port:2019}")
    private int port;

    @Value("${spring.enode.server.wait.timeout:10000}")
    private int timeout;

    @Bean(name = "defaultCommandResultProcessor")
    @ConditionalOnProperty(prefix = "spring.enode", name = "server.port")
    public DefaultCommandResultProcessor defaultCommandResultProcessor(IScheduleService scheduleService, ISerializeService serializeService) {
        DefaultCommandResultProcessor processor = new DefaultCommandResultProcessor(scheduleService, serializeService, port, timeout);
        return processor;
    }

    @Bean(name = "defaultSendReplyService")
    public DefaultSendReplyService defaultSendReplyService(ISerializeService serializeService) {
        DefaultSendReplyService sendReplyService = new DefaultSendReplyService(serializeService);
        return sendReplyService;
    }

    @Bean(name = "defaultScheduleService")
    public DefaultScheduleService defaultScheduleService() {
        return new DefaultScheduleService();
    }

    @Bean(name = "defaultTypeNameProvider")
    public DefaultTypeNameProvider defaultTypeNameProvider() {
        return new DefaultTypeNameProvider(Maps.newHashMap());
    }

    @Bean(name = "defaultProcessingEventProcessor", initMethod = "start", destroyMethod = "stop")
    public DefaultProcessingEventProcessor defaultProcessingEventProcessor(IScheduleService scheduleService, ISerializeService serializeService, IMessageDispatcher messageDispatcher, IPublishedVersionStore publishedVersionStore) {
        return new DefaultProcessingEventProcessor(scheduleService, serializeService, messageDispatcher, publishedVersionStore);
    }

    @Bean(name = "defaultEventSerializer")
    public DefaultEventSerializer defaultEventSerializer(ITypeNameProvider typeNameProvider, ISerializeService serializeService) {
        return new DefaultEventSerializer(typeNameProvider, serializeService);
    }

    @Bean(name = "defaultAggregateRootInternalHandlerProvider")
    public DefaultAggregateRootInternalHandlerProvider defaultAggregateRootInternalHandlerProvider() {
        return new DefaultAggregateRootInternalHandlerProvider();
    }

    @Bean(name = "defaultMessageDispatcher")
    public DefaultMessageDispatcher defaultMessageDispatcher(
            ITypeNameProvider typeNameProvider,
            IMessageHandlerProvider messageHandlerProvider,
            ITwoMessageHandlerProvider twoMessageHandlerProvider,
            IThreeMessageHandlerProvider threeMessageHandlerProvider,
            ISerializeService serializeService
    ) {
        return new DefaultMessageDispatcher(typeNameProvider, messageHandlerProvider, twoMessageHandlerProvider, threeMessageHandlerProvider, serializeService);
    }

    @Bean(name = "defaultRepository")
    public DefaultRepository defaultRepository(IMemoryCache memoryCache) {
        return new DefaultRepository(memoryCache);
    }

    @Bean(name = "defaultMemoryCache", initMethod = "start", destroyMethod = "stop")
    public DefaultMemoryCache defaultMemoryCache(IAggregateStorage aggregateStorage, IScheduleService scheduleService, ITypeNameProvider typeNameProvider) {
        return new DefaultMemoryCache(aggregateStorage, scheduleService, typeNameProvider);
    }

    @Bean(name = "defaultAggregateRepositoryProvider")
    public DefaultAggregateRepositoryProvider defaultAggregateRepositoryProvider() {
        return new DefaultAggregateRepositoryProvider();
    }

    @Bean(name = "defaultThreeMessageHandlerProvider")
    public DefaultThreeMessageHandlerProvider defaultThreeMessageHandlerProvider() {
        return new DefaultThreeMessageHandlerProvider();
    }

    @Bean(name = "defaultTwoMessageHandlerProvider")
    public DefaultTwoMessageHandlerProvider defaultTwoMessageHandlerProvider() {
        return new DefaultTwoMessageHandlerProvider();
    }

    @Bean(name = "defaultMessageHandlerProvider")
    public DefaultMessageHandlerProvider defaultMessageHandlerProvider() {
        return new DefaultMessageHandlerProvider();
    }

    @Bean(name = "defaultCommandHandlerProvider")
    public DefaultCommandHandlerProvider defaultCommandHandlerProvider() {
        return new DefaultCommandHandlerProvider();
    }

    @Bean(name = "defaultAggregateRootFactory")
    public DefaultAggregateRootFactory defaultAggregateRootFactory() {
        return new DefaultAggregateRootFactory();
    }

    @Bean(name = "defaultAggregateSnapshotter")
    public DefaultAggregateSnapshotter defaultAggregateSnapshotter(IAggregateRepositoryProvider aggregateRepositoryProvider) {
        return new DefaultAggregateSnapshotter(aggregateRepositoryProvider);
    }

    @Bean(name = "defaultProcessingCommandHandler")
    public DefaultProcessingCommandHandler defaultProcessingCommandHandler(
            IEventStore eventStore,
            ICommandHandlerProvider commandHandlerProvider,
            ITypeNameProvider typeNameProvider,
            IEventCommittingService eventService,
            IMemoryCache memoryCache,
            @Qualifier(value = "defaultApplicationMessagePublisher") IMessagePublisher<IApplicationMessage> applicationMessagePublisher,
            @Qualifier(value = "defaultPublishableExceptionPublisher") IMessagePublisher<IDomainException> publishableExceptionPublisher,
            ISerializeService serializeService) {
        return new DefaultProcessingCommandHandler(eventStore, commandHandlerProvider, typeNameProvider, eventService, memoryCache, applicationMessagePublisher, publishableExceptionPublisher, serializeService);
    }

    @Bean(name = "defaultEventCommittingService")
    public DefaultEventCommittingService defaultEventCommittingService(
            IMemoryCache memoryCache,
            IEventStore eventStore,
            ISerializeService serializeService,
            @Qualifier("defaultDomainEventPublisher") IMessagePublisher<DomainEventStreamMessage> domainEventPublisher) {
        return new DefaultEventCommittingService(memoryCache, eventStore, serializeService, domainEventPublisher);
    }

    @Bean(name = "defaultSerializeService")
    @ConditionalOnProperty(prefix = "spring.enode", name = "serialize", havingValue = "jackson", matchIfMissing = true)
    public DefaultSerializeService defaultSerializeService() {
        return new DefaultSerializeService();
    }

    @Bean(name = "defaultCommandProcessor", initMethod = "start", destroyMethod = "stop")
    public DefaultCommandProcessor defaultCommandProcessor(IProcessingCommandHandler processingCommandHandler, IScheduleService scheduleService) {
        return new DefaultCommandProcessor(processingCommandHandler, scheduleService);
    }

    @Bean(name = "snapshotOnlyAggregateStorage")
    @ConditionalOnProperty(prefix = "spring.enode", name = "aggregatestorage", havingValue = "snapshot", matchIfMissing = false)
    public SnapshotOnlyAggregateStorage snapshotOnlyAggregateStorage(IAggregateSnapshotter aggregateSnapshotter) {
        return new SnapshotOnlyAggregateStorage(aggregateSnapshotter);
    }

    @Bean(name = "eventSourcingAggregateStorage")
    @ConditionalOnProperty(prefix = "spring.enode", name = "aggregatestorage", havingValue = "eventsourcing", matchIfMissing = true)
    public EventSourcingAggregateStorage eventSourcingAggregateStorage(
            IAggregateRootFactory aggregateRootFactory,
            IEventStore eventStore,
            IAggregateSnapshotter aggregateSnapshotter,
            ITypeNameProvider typeNameProvider) {
        return new EventSourcingAggregateStorage(eventStore, aggregateRootFactory, aggregateSnapshotter, typeNameProvider);
    }

    @Bean(name = "defaultCommandService")
    public DefaultCommandService defaultCommandService(ICommandResultProcessor commandResultProcessor, ISendMessageService sendMessageService, ISerializeService serializeService) {
        return new DefaultCommandService(commandTopic, commandTag, commandResultProcessor, sendMessageService, serializeService);
    }

    @Bean(name = "defaultDomainEventPublisher")
    public DefaultDomainEventPublisher defaultDomainEventPublisher(IEventSerializer eventSerializer, ISendMessageService sendMessageService, ISerializeService serializeService) {
        return new DefaultDomainEventPublisher(eventTopic, eventTag, eventSerializer, sendMessageService, serializeService);
    }

    @Bean(name = "defaultApplicationMessagePublisher")
    public DefaultApplicationMessagePublisher defaultApplicationMessagePublisher(ISendMessageService sendMessageService, ISerializeService serializeService) {
        return new DefaultApplicationMessagePublisher(applicationTopic, applicationTag, sendMessageService, serializeService);
    }

    @Bean(name = "defaultPublishableExceptionPublisher")
    public DefaultPublishableExceptionPublisher defaultPublishableExceptionPublisher(ISendMessageService sendMessageService, ISerializeService serializeService) {
        return new DefaultPublishableExceptionPublisher(exceptionTopic, eventTag, sendMessageService, serializeService);
    }

    @Bean(name = "defaultCommandMessageHandler")
    public DefaultCommandMessageHandler defaultCommandMessageHandler(ISendReplyService sendReplyService, ITypeNameProvider typeNameProvider, ICommandProcessor commandProcessor, IRepository repository, IAggregateStorage aggregateRootStorage, ISerializeService serializeService) {
        return new DefaultCommandMessageHandler(sendReplyService, typeNameProvider, commandProcessor, repository, aggregateRootStorage, serializeService);
    }

    @Bean(name = "defaultDomainEventMessageHandler")
    public DefaultDomainEventMessageHandler defaultDomainEventMessageHandler(ISendReplyService sendReplyService, IProcessingEventProcessor domainEventMessageProcessor, IEventSerializer eventSerializer, ISerializeService serializeService) {
        return new DefaultDomainEventMessageHandler(sendReplyService, domainEventMessageProcessor, eventSerializer, serializeService);
    }

    @Bean(name = "defaultPublishableExceptionMessageHandler")
    public DefaultPublishableExceptionMessageHandler defaultPublishableExceptionMessageHandler(ITypeNameProvider typeNameProvider, IMessageDispatcher messageDispatcher, ISerializeService serializeService) {
        return new DefaultPublishableExceptionMessageHandler(typeNameProvider, messageDispatcher, serializeService);
    }

    @Bean(name = "defaultApplicationMessageHandler")
    public DefaultApplicationMessageHandler defaultApplicationMessageHandler(ITypeNameProvider typeNameProvider, IMessageDispatcher messageDispatcher, ISerializeService serializeService) {
        return new DefaultApplicationMessageHandler(typeNameProvider, messageDispatcher, serializeService);
    }
}
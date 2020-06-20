package org.enodeframework.spring;

import com.mongodb.reactivestreams.client.MongoClient;
import org.enodeframework.commanding.ICommandHandlerProvider;
import org.enodeframework.commanding.ICommandProcessor;
import org.enodeframework.commanding.IProcessingCommandHandler;
import org.enodeframework.commanding.impl.CommandHandlerProxy;
import org.enodeframework.commanding.impl.DefaultCommandHandlerProvider;
import org.enodeframework.commanding.impl.DefaultCommandProcessor;
import org.enodeframework.commanding.impl.DefaultProcessingCommandHandler;
import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.common.extensions.ClassNameComparator;
import org.enodeframework.common.extensions.ClassPathScanHandler;
import org.enodeframework.common.scheduling.IScheduleService;
import org.enodeframework.common.scheduling.ScheduleService;
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
import org.enodeframework.eventing.impl.InMemoryEventStore;
import org.enodeframework.eventing.impl.InMemoryPublishedVersionStore;
import org.enodeframework.infrastructure.IAssemblyInitializer;
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
import org.enodeframework.messaging.impl.MessageHandlerProxy1;
import org.enodeframework.messaging.impl.MessageHandlerProxy2;
import org.enodeframework.messaging.impl.MessageHandlerProxy3;
import org.enodeframework.mongo.MongoEventStore;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.ISendReplyService;
import org.enodeframework.queue.applicationmessage.DefaultApplicationMessageListener;
import org.enodeframework.queue.applicationmessage.DefaultApplicationMessagePublisher;
import org.enodeframework.queue.command.DefaultCommandListener;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
import org.enodeframework.queue.command.DefaultCommandService;
import org.enodeframework.queue.command.ICommandResultProcessor;
import org.enodeframework.queue.domainevent.DefaultDomainEventListener;
import org.enodeframework.queue.domainevent.DefaultDomainEventPublisher;
import org.enodeframework.queue.publishableexceptions.DefaultPublishableExceptionListener;
import org.enodeframework.queue.publishableexceptions.DefaultPublishableExceptionPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author anruence@gmail.com
 */
public class EnodeAutoConfiguration implements ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(EnodeAutoConfiguration.class);

    private ApplicationContext applicationContext;

    @Value("${spring.enode.queue.command.topic}")
    private String commandTopic;

    @Value("${spring.enode.queue.event.topic}")
    private String eventTopic;

    @Value("${spring.enode.queue.application.topic}")
    private String applicationTopic;

    @Value("${spring.enode.queue.exception.topic}")
    private String exceptionTopic;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        SpringObjectContainer container = new SpringObjectContainer(applicationContext);
        ObjectContainer.INSTANCE = container;
        scanConfiguredPackages(ObjectContainer.BASE_PACKAGES);
    }

    private void registerBeans(Set<Class<?>> classSet) {
        applicationContext.getBeansOfType(IAssemblyInitializer.class).values().forEach(provider -> {
            provider.initialize(classSet);
            if (logger.isDebugEnabled()) {
                logger.debug("{} initial success", provider.getClass().getName());
            }
        });
    }

    /**
     * Scan the packages configured
     */
    private void scanConfiguredPackages(String... scanPackages) {
        if (scanPackages == null) {
            throw new IllegalArgumentException("packages is not specified");
        }
        ClassPathScanHandler handler = new ClassPathScanHandler(scanPackages);
        Set<Class<?>> classSet = new TreeSet<>(new ClassNameComparator());
        for (String pakName : scanPackages) {
            classSet.addAll(handler.getPackageAllClasses(pakName, true));
        }
        registerBeans(classSet);
    }

    @Bean(name = "scheduleService")
    public ScheduleService scheduleService() {
        return new ScheduleService();
    }

    @Bean(name = "typeNameProvider")
    public DefaultTypeNameProvider defaultTypeNameProvider() {
        return new DefaultTypeNameProvider();
    }

    @Bean(name = "domainEventMessageProcessor", initMethod = "start", destroyMethod = "stop")
    public DefaultProcessingEventProcessor domainEventMessageProcessor(IScheduleService scheduleService, IMessageDispatcher messageDispatcher, IPublishedVersionStore publishedVersionStore) {
        return new DefaultProcessingEventProcessor(scheduleService, messageDispatcher, publishedVersionStore);
    }

    /**
     * 原型模式获取bean，每次新建代理执行
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CommandHandlerProxy commandHandlerProxy() {
        return new CommandHandlerProxy();
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

    @Bean(name = "eventSerializer")
    public DefaultEventSerializer defaultEventSerializer(ITypeNameProvider typeNameProvider) {
        return new DefaultEventSerializer(typeNameProvider);
    }

    @Bean(name = "aggregateRootInternalHandlerProvider")
    public DefaultAggregateRootInternalHandlerProvider aggregateRootInternalHandlerProvider() {
        return new DefaultAggregateRootInternalHandlerProvider();
    }

    @Bean(name = "messageDispatcher")
    public DefaultMessageDispatcher defaultMessageDispatcher(
            ITypeNameProvider typeNameProvider,
            IMessageHandlerProvider messageHandlerProvider,
            ITwoMessageHandlerProvider twoMessageHandlerProvider,
            IThreeMessageHandlerProvider threeMessageHandlerProvider) {
        return new DefaultMessageDispatcher(typeNameProvider, messageHandlerProvider, twoMessageHandlerProvider, threeMessageHandlerProvider);
    }

    @Bean(name = "repository")
    public DefaultRepository defaultRepository(IMemoryCache memoryCache) {
        return new DefaultRepository(memoryCache);
    }

    @Bean(name = "memoryCache", initMethod = "start", destroyMethod = "stop")
    public DefaultMemoryCache defaultMemoryCache(IAggregateStorage aggregateStorage, IScheduleService scheduleService, ITypeNameProvider typeNameProvider) {
        return new DefaultMemoryCache(aggregateStorage, scheduleService, typeNameProvider);
    }

    @Bean(name = "aggregateRepositoryProvider")
    public DefaultAggregateRepositoryProvider aggregateRepositoryProvider() {
        return new DefaultAggregateRepositoryProvider();
    }

    @Bean(name = "threeMessageHandlerProvider")
    public DefaultThreeMessageHandlerProvider threeMessageHandlerProvider() {
        return new DefaultThreeMessageHandlerProvider();
    }

    @Bean(name = "twoMessageHandlerProvider")
    public DefaultTwoMessageHandlerProvider twoMessageHandlerProvider() {
        return new DefaultTwoMessageHandlerProvider();
    }

    @Bean(name = "messageHandlerProvider")
    public DefaultMessageHandlerProvider messageHandlerProvider() {
        return new DefaultMessageHandlerProvider();
    }

    @Bean(name = "commandHandlerProvider")
    public DefaultCommandHandlerProvider commandHandlerProvider() {
        return new DefaultCommandHandlerProvider();
    }

    @Bean(name = "aggregateRootFactory")
    public DefaultAggregateRootFactory aggregateRootFactory() {
        return new DefaultAggregateRootFactory();
    }

    @Bean(name = "aggregateSnapshotter")
    public DefaultAggregateSnapshotter aggregateSnapshotter(IAggregateRepositoryProvider aggregateRepositoryProvider) {
        return new DefaultAggregateSnapshotter(aggregateRepositoryProvider);
    }

    @Bean
    public DefaultProcessingCommandHandler defaultProcessingCommandHandler(
            IEventStore eventStore,
            ICommandHandlerProvider commandHandlerProvider,
            ITypeNameProvider typeNameProvider,
            IEventCommittingService eventService,
            IMemoryCache memoryCache,
            @Qualifier(value = "applicationMessagePublisher") IMessagePublisher<IApplicationMessage> applicationMessagePublisher,
            @Qualifier(value = "publishableExceptionPublisher") IMessagePublisher<IDomainException> publishableExceptionPublisher
    ) {
        return new DefaultProcessingCommandHandler(eventStore, commandHandlerProvider, typeNameProvider, eventService, memoryCache, applicationMessagePublisher, publishableExceptionPublisher);
    }

    @Bean
    public DefaultEventCommittingService defaultEventService(IMemoryCache memoryCache, IEventStore eventStore, @Qualifier(value = "domainEventPublisher") IMessagePublisher<DomainEventStreamMessage> domainEventPublisher) {
        return new DefaultEventCommittingService(memoryCache, eventStore, domainEventPublisher);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DefaultCommandProcessor defaultCommandProcessor(IProcessingCommandHandler processingCommandHandler, IScheduleService scheduleService) {
        return new DefaultCommandProcessor(processingCommandHandler, scheduleService);
    }

    @Bean
    public DefaultCommandResultProcessor defaultCommandResultProcessor() {
        return new DefaultCommandResultProcessor();
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

    @Bean
    public DefaultCommandService defaultCommandService(ICommandResultProcessor commandResultProcessor, ISendMessageService sendMessageService) {
        return new DefaultCommandService(commandTopic, commandResultProcessor, sendMessageService);
    }

    @Bean(name = "domainEventPublisher")
    public DefaultDomainEventPublisher domainEventPublisher(IEventSerializer eventSerializer, ISendMessageService sendMessageService) {
        return new DefaultDomainEventPublisher(eventTopic, eventSerializer, sendMessageService);
    }

    @Bean(name = "applicationMessagePublisher")
    public DefaultApplicationMessagePublisher applicationMessagePublisher(ISendMessageService sendMessageService) {
        return new DefaultApplicationMessagePublisher(eventTopic, sendMessageService);
    }

    @Bean(name = "publishableExceptionPublisher")
    public DefaultPublishableExceptionPublisher publishableExceptionPublisher(ISendMessageService sendMessageService) {
        return new DefaultPublishableExceptionPublisher(eventTopic, sendMessageService);
    }

    @Bean(name = "defaultCommandListener")
    public DefaultCommandListener commandListener(ISendReplyService sendReplyService, ITypeNameProvider typeNameProvider, ICommandProcessor commandProcessor, IRepository repository, IAggregateStorage aggregateRootStorage) {
        return new DefaultCommandListener(sendReplyService, typeNameProvider, commandProcessor, repository, aggregateRootStorage);
    }

    @Bean(name = "defaultDomainEventListener")
    public DefaultDomainEventListener domainEventListener(ISendReplyService sendReplyService, IProcessingEventProcessor domainEventMessageProcessor, IEventSerializer eventSerializer) {
        return new DefaultDomainEventListener(sendReplyService, domainEventMessageProcessor, eventSerializer);
    }

    @Bean(name = "defaultPublishableExceptionListener")
    public DefaultPublishableExceptionListener publishableExceptionListener(ITypeNameProvider typeNameProvider, IMessageDispatcher messageDispatcher) {
        return new DefaultPublishableExceptionListener(typeNameProvider, messageDispatcher);
    }

    @Bean(name = "defaultApplicationMessageListener")
    public DefaultApplicationMessageListener applicationMessageListener(ITypeNameProvider typeNameProvider, IMessageDispatcher messageDispatcher) {
        return new DefaultApplicationMessageListener(typeNameProvider, messageDispatcher);
    }
}
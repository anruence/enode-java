package com.enode;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandAsyncHandler;
import com.enode.commanding.ICommandAsyncHandlerProvider;
import com.enode.commanding.ICommandHandler;
import com.enode.commanding.ICommandHandlerProvider;
import com.enode.commanding.ICommandProcessor;
import com.enode.commanding.ICommandRoutingKeyProvider;
import com.enode.commanding.ICommandService;
import com.enode.commanding.IProcessingCommandHandler;
import com.enode.commanding.impl.DefaultCommandAsyncHandlerProvider;
import com.enode.commanding.impl.DefaultCommandHandlerProvider;
import com.enode.commanding.impl.DefaultCommandProcessor;
import com.enode.commanding.impl.DefaultCommandRoutingKeyProvider;
import com.enode.commanding.impl.DefaultProcessingCommandHandler;
import com.enode.commanding.impl.NotImplementedCommandService;
import com.enode.common.container.AbstractContainer;
import com.enode.common.container.GenericTypeLiteral;
import com.enode.common.container.IObjectContainer;
import com.enode.common.container.LifeStyle;
import com.enode.common.io.IOHelper;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.scheduling.IScheduleService;
import com.enode.common.scheduling.ScheduleService;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.thirdparty.gson.GsonJsonSerializer;
import com.enode.common.thirdparty.guice.GuiceObjectContainer;
import com.enode.configurations.ConfigurationSetting;
import com.enode.configurations.OptionSetting;
import com.enode.domain.AggregateRoot;
import com.enode.domain.IAggregateRepository;
import com.enode.domain.IAggregateRepositoryProvider;
import com.enode.domain.IAggregateRootFactory;
import com.enode.domain.IAggregateRootInternalHandlerProvider;
import com.enode.domain.IAggregateSnapshotter;
import com.enode.domain.IAggregateStorage;
import com.enode.domain.IMemoryCache;
import com.enode.domain.IRepository;
import com.enode.domain.impl.DefaultAggregateRepositoryProvider;
import com.enode.domain.impl.DefaultAggregateRootFactory;
import com.enode.domain.impl.DefaultAggregateRootInternalHandlerProvider;
import com.enode.domain.impl.DefaultAggregateSnapshotter;
import com.enode.domain.impl.DefaultMemoryCache;
import com.enode.domain.impl.DefaultRepository;
import com.enode.domain.impl.EventSourcingAggregateStorage;
import com.enode.domain.impl.SnapshotOnlyAggregateStorage;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.eventing.IEventSerializer;
import com.enode.eventing.IEventService;
import com.enode.eventing.IEventStore;
import com.enode.eventing.impl.DefaultEventSerializer;
import com.enode.eventing.impl.DefaultEventService;
import com.enode.eventing.impl.DomainEventStreamMessageHandler;
import com.enode.eventing.impl.InMemoryEventStore;
import com.enode.eventing.impl.MysqlEventStore;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IAssemblyInitializer;
import com.enode.infrastructure.ILockService;
import com.enode.infrastructure.IMessageDispatcher;
import com.enode.infrastructure.IMessageHandler;
import com.enode.infrastructure.IMessageHandlerProvider;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.infrastructure.IProcessingMessageHandler;
import com.enode.infrastructure.IProcessingMessageScheduler;
import com.enode.infrastructure.IPublishableException;
import com.enode.infrastructure.IPublishedVersionStore;
import com.enode.infrastructure.IThreeMessageHandlerProvider;
import com.enode.infrastructure.ITimeProvider;
import com.enode.infrastructure.ITwoMessageHandlerProvider;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.LifeStyleType;
import com.enode.infrastructure.ProcessingApplicationMessage;
import com.enode.infrastructure.ProcessingDomainEventStreamMessage;
import com.enode.infrastructure.ProcessingPublishableExceptionMessage;
import com.enode.infrastructure.TypeUtils;
import com.enode.infrastructure.impl.DefaultApplicationMessageProcessor;
import com.enode.infrastructure.impl.DefaultDomainEventProcessor;
import com.enode.infrastructure.impl.DefaultMessageDispatcher;
import com.enode.infrastructure.impl.DefaultMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultProcessingMessageHandler;
import com.enode.infrastructure.impl.DefaultProcessingMessageScheduler;
import com.enode.infrastructure.impl.DefaultPublishableExceptionProcessor;
import com.enode.infrastructure.impl.DefaultThreeMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultTimeProvider;
import com.enode.infrastructure.impl.DefaultTwoMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultTypeNameProvider;
import com.enode.infrastructure.impl.DoNothingPublisher;
import com.enode.infrastructure.impl.inmemory.InMemoryPublishedVersionStore;
import com.enode.infrastructure.impl.mysql.MysqlLockService;
import com.enode.infrastructure.impl.mysql.MysqlPublishedVersionStore;
import com.enode.jmx.ENodeJMXAgent;
import com.enode.kafka.ConsumeKafkaService;
import com.enode.kafka.SendKafkaService;
import com.enode.rocketmq.IMQConsumer;
import com.enode.rocketmq.IMQProducer;
import com.enode.rocketmq.ITopicProvider;
import com.enode.rocketmq.RocketMQConsumer;
import com.enode.rocketmq.SendReplyService;
import com.enode.rocketmq.SendRocketMQService;
import com.enode.rocketmq.TopicData;
import com.enode.rocketmq.applicationmessage.ApplicationMessageConsumer;
import com.enode.rocketmq.applicationmessage.ApplicationMessagePublisher;
import com.enode.rocketmq.client.Consumer;
import com.enode.rocketmq.client.Producer;
import com.enode.rocketmq.client.RocketMQFactory;
import com.enode.rocketmq.client.impl.NativeMQFactory;
import com.enode.rocketmq.client.ons.ONSFactory;
import com.enode.rocketmq.command.CommandConsumer;
import com.enode.rocketmq.command.CommandResultProcessor;
import com.enode.rocketmq.command.CommandService;
import com.enode.rocketmq.domainevent.DomainEventConsumer;
import com.enode.rocketmq.domainevent.DomainEventPublisher;
import com.enode.rocketmq.publishableexceptions.PublishableExceptionConsumer;
import com.enode.rocketmq.publishableexceptions.PublishableExceptionPublisher;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ENode extends AbstractContainer<ENode> {

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
    private static final String[] ENODE_PACKAGE_SCAN = new String[]{"com.enode.domain", "com.enode.message", "com.enode.infrastructure.impl"};
    private static final Set<Class> ENODE_COMPONENT_TYPES = new HashSet<Class>() {{
        add(ICommandHandler.class);
        add(ICommandAsyncHandler.class);
        add(IMessageHandler.class);
        add(IAggregateRepository.class);
        add(ITopicProvider.class);
    }};
    private static final Set<Class> ENODE_GENERIC_COMPONENT_TYPES = new HashSet<Class>() {{
        add(ITopicProvider.class);
    }};
    private static ENode instance;
    private List<Class<?>> _assemblyInitializerServiceTypes;
    private String[] scanPackages;
    private Set<Class<?>> assemblyTypes;
    private ConfigurationSetting setting;
    private int registerMQFlag;

    private ENode(ConfigurationSetting setting, String... packages) {
        this.setting = setting == null ? new ConfigurationSetting() : setting;
        this.scanPackages = packages;
        _assemblyInitializerServiceTypes = new ArrayList<>();

        scanAssemblyTypes();
    }

    public static ENode getInstance() {
        return instance;
    }

    public static ENode create(String... packages) {
        return create(null, packages);
    }

    public static ENode create(ConfigurationSetting setting, String... packages) {
        instance = new ENode(setting, packages);
        return instance;
    }

    private static LifeStyle parseComponentLife(Class type) {
        LifeStyleType annotation = (LifeStyleType) type.getAnnotation(LifeStyleType.class);

        if (annotation != null) {
            return annotation.value();
        }

        return LifeStyle.Singleton;
    }

    private static boolean isAssemblyInitializer(Class type) {
        return !Modifier.isAbstract(type.getModifiers()) && IAssemblyInitializer.class.isAssignableFrom(type);
    }

    public ENode useGuice() {
        GuiceObjectContainer objectContainer = new GuiceObjectContainer();
        //设置当前ENode实例的依赖注入容器
        super.setContainer(objectContainer);
        //注入容器本身，为了使ENode彻底支持多实例，将容器本身注入，原先引用ObjectContainer调用静态方法的地方，可以直接依赖IObjectContainer实例方法
        registerInstance(IObjectContainer.class, objectContainer);
        return this;
    }

    public ConfigurationSetting getSetting() {
        return setting;
    }

    public ENode registerProperties(String propertiesResourceFile) {
        InputStream propertiesResource = this.getClass().getClassLoader().getResourceAsStream(propertiesResourceFile);
        Properties properties = new Properties();
        try {
            properties.load(propertiesResource);
            return registerProperties(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ENode registerCommonComponents() {
        register(IJsonSerializer.class, GsonJsonSerializer.class);
        register(IScheduleService.class, ScheduleService.class);
        register(IOHelper.class);
        return this;
    }

    public ENode registerENodeComponents() {
        register(ITimeProvider.class, DefaultTimeProvider.class);
        register(ITypeNameProvider.class, DefaultTypeNameProvider.class);
        register(IMessageHandlerProvider.class, DefaultMessageHandlerProvider.class);
        register(ITwoMessageHandlerProvider.class, DefaultTwoMessageHandlerProvider.class);
        register(IThreeMessageHandlerProvider.class, DefaultThreeMessageHandlerProvider.class);

        register(IAggregateRootInternalHandlerProvider.class, DefaultAggregateRootInternalHandlerProvider.class);
        register(IAggregateRepositoryProvider.class, DefaultAggregateRepositoryProvider.class);
        register(IAggregateRootFactory.class, DefaultAggregateRootFactory.class);
        register(IMemoryCache.class, DefaultMemoryCache.class);
        register(IAggregateSnapshotter.class, DefaultAggregateSnapshotter.class);
        register(IAggregateStorage.class, EventSourcingAggregateStorage.class);
        register(IRepository.class, DefaultRepository.class);

        register(ICommandAsyncHandlerProvider.class, DefaultCommandAsyncHandlerProvider.class);
        register(ICommandHandlerProvider.class, DefaultCommandHandlerProvider.class);
        register(ICommandRoutingKeyProvider.class, DefaultCommandRoutingKeyProvider.class);
        register(ICommandService.class, NotImplementedCommandService.class);

        register(IEventSerializer.class, DefaultEventSerializer.class);
        register(IEventStore.class, InMemoryEventStore.class);
        register(IPublishedVersionStore.class, InMemoryPublishedVersionStore.class);
        register(IEventService.class, DefaultEventService.class);

        register(IMessageDispatcher.class, DefaultMessageDispatcher.class);


        register(new GenericTypeLiteral<IMessagePublisher<IApplicationMessage>>() {
        }, DoNothingPublisher.class);
        register(new GenericTypeLiteral<IMessagePublisher<DomainEventStreamMessage>>() {
        }, DoNothingPublisher.class);
        register(new GenericTypeLiteral<IMessagePublisher<IPublishableException>>() {
        }, DoNothingPublisher.class);

        register(IProcessingCommandHandler.class, DefaultProcessingCommandHandler.class);
        register(new GenericTypeLiteral<IProcessingMessageHandler<ProcessingApplicationMessage, IApplicationMessage>>() {
        }, new GenericTypeLiteral<DefaultProcessingMessageHandler<ProcessingApplicationMessage, IApplicationMessage>>() {
        }, null, LifeStyle.Singleton);
        register(new GenericTypeLiteral<IProcessingMessageHandler<ProcessingDomainEventStreamMessage, DomainEventStreamMessage>>() {
        }, DomainEventStreamMessageHandler.class);
        register(new GenericTypeLiteral<IProcessingMessageHandler<ProcessingPublishableExceptionMessage, IPublishableException>>() {
        }, new GenericTypeLiteral<DefaultProcessingMessageHandler<ProcessingPublishableExceptionMessage, IPublishableException>>() {
        }, null, LifeStyle.Singleton);

        register(new GenericTypeLiteral<IProcessingMessageScheduler<ProcessingApplicationMessage, IApplicationMessage>>() {
        }, new GenericTypeLiteral<DefaultProcessingMessageScheduler<ProcessingApplicationMessage, IApplicationMessage>>() {
        }, null, LifeStyle.Singleton);
        register(new GenericTypeLiteral<IProcessingMessageScheduler<ProcessingDomainEventStreamMessage, DomainEventStreamMessage>>() {
        }, new GenericTypeLiteral<DefaultProcessingMessageScheduler<ProcessingDomainEventStreamMessage, DomainEventStreamMessage>>() {
        }, null, LifeStyle.Singleton);
        register(new GenericTypeLiteral<IProcessingMessageScheduler<ProcessingPublishableExceptionMessage, IPublishableException>>() {
        }, new GenericTypeLiteral<DefaultProcessingMessageScheduler<ProcessingPublishableExceptionMessage, IPublishableException>>() {
        }, null, LifeStyle.Singleton);


        register(ICommandProcessor.class, DefaultCommandProcessor.class);

        register(new GenericTypeLiteral<IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage>>() {
        }, DefaultApplicationMessageProcessor.class);
        register(new GenericTypeLiteral<IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage>>() {
        }, DefaultDomainEventProcessor.class);
        register(new GenericTypeLiteral<IMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException>>() {
        }, DefaultPublishableExceptionProcessor.class);

        registerStaticInjection(AggregateRoot.class);

        _assemblyInitializerServiceTypes.add(IAggregateRootInternalHandlerProvider.class);
        _assemblyInitializerServiceTypes.add(IAggregateRepositoryProvider.class);
        _assemblyInitializerServiceTypes.add(IMessageHandlerProvider.class);
        _assemblyInitializerServiceTypes.add(ITwoMessageHandlerProvider.class);
        _assemblyInitializerServiceTypes.add(IThreeMessageHandlerProvider.class);
        _assemblyInitializerServiceTypes.add(ICommandHandlerProvider.class);
        _assemblyInitializerServiceTypes.add(ICommandAsyncHandlerProvider.class);
        return this;
    }

    public ENode registerBusinessComponents() {
        assemblyTypes.stream().filter(type -> TypeUtils.isComponent(type) || isENodeComponentType(type)).forEach(this::registerComponentType);
        return this;
    }

    public ENode useSnapshotOnlyAggregateStorage() {
        register(IAggregateStorage.class, SnapshotOnlyAggregateStorage.class);
        return this;
    }

    public ENode useMysqlComponents(DataSource ds, OptionSetting optionSetting) {
        return useMysqlLockService(ds, null)
                .useMysqlEventStore(ds, null)
                .useMysqlPublishedVersionStore(ds, null);
    }

    public ENode useMysqlLockService(DataSource ds, OptionSetting optionSetting) {
        registerInstance(ILockService.class, new MysqlLockService(ds, optionSetting));

        return this;
    }

    public ENode useMysqlEventStore(DataSource ds, OptionSetting optionSetting) {
        //TODO primary key name,index name,bulk copy property
        register(IEventStore.class, null, () -> new MysqlEventStore(ds, optionSetting, getContainer()), LifeStyle.Singleton);
        return this;
    }

    public ENode useMysqlPublishedVersionStore(DataSource ds, OptionSetting optionSetting) {
        register(IPublishedVersionStore.class, null, () -> new MysqlPublishedVersionStore(ds, optionSetting), LifeStyle.Singleton);

        return this;
    }

    private void registerComponentType(Class type) {
        LifeStyle life = parseComponentLife(type);
        register(type, null, life);
        if (isENodeGenericComponentType(type)) {
            registerENodeGenericComponentType(type);
        }
        if (isAssemblyInitializer(type)) {
            _assemblyInitializerServiceTypes.add(type);
        }
    }

    private void registerENodeGenericComponentType(Class type) {
        List<Class> superInterfaces = ENODE_GENERIC_COMPONENT_TYPES.stream().filter(x -> x.isAssignableFrom(type)).collect(Collectors.toList());

        superInterfaces.forEach(superInterface -> {
            Type superGenericInterface = TypeUtils.getSuperGenericInterface(type, superInterface);
            if (superGenericInterface != null) {
                register(GenericTypeLiteral.get(superGenericInterface), type);
            }
        });
    }

    private ENode initializeBusinessAssemblies() {
        _assemblyInitializerServiceTypes.stream()
                .map(x -> (IAssemblyInitializer) resolve(x))
                .forEach(x -> x.initialize(assemblyTypes));

        return this;
    }

    private boolean isENodeComponentType(Class type) {
        if (Modifier.isAbstract(type.getModifiers())) {
            return false;
        }

        return ENODE_COMPONENT_TYPES.stream().anyMatch(x -> x.isAssignableFrom(type));
    }

    private boolean isENodeGenericComponentType(Class type) {
        if (Modifier.isAbstract(type.getModifiers())) {
            return false;
        }

        return ENODE_GENERIC_COMPONENT_TYPES.stream().anyMatch(x -> x.isAssignableFrom(type));
    }

    public ENode registerDefaultComponents() {
        return useGuice()
                .registerCommonComponents()
                .registerENodeComponents()
                .registerBusinessComponents();
    }

    public ENode useONS(Properties producerSetting, Properties consumerSetting, int listenPort, int registerMQFlag) {
        return useMQ(producerSetting, consumerSetting, registerMQFlag, listenPort, TYPE_ONS);
    }

    public ENode useKafka(Properties producerSetting, Properties consumerSetting, int listenPort, int registerMQFlag) {
        return useMQ(producerSetting, consumerSetting, registerMQFlag, listenPort, TYPE_KAFKA);
    }

    public ENode useNativeRocketMQ(Properties producerSetting, Properties consumerSetting, int listenPort, int registerMQFlag) {
        return useMQ(producerSetting, consumerSetting, registerMQFlag, listenPort, TYPE_ROCKETMQ);
    }

    private ENode useMQ(Properties producerSetting, Properties consumerSetting, int registerMQFlag, int listenPort, int mqType) {
        this.registerMQFlag = registerMQFlag;
        RocketMQFactory mqFactory = null;
        if (mqType == TYPE_ONS) {
            mqFactory = new ONSFactory();
        } else if (mqType == TYPE_ROCKETMQ) {
            mqFactory = new NativeMQFactory();
        }
        //Create MQConsumer and any register consumers(CommandConsumer、DomainEventConsumer、ApplicationMessageConsumer、PublishableExceptionConsumer)
        if (hasAnyComponents(registerMQFlag, CONSUMERS)) {
            if (mqType == TYPE_KAFKA) {
                KafkaConsumer kafkaConsumer = new KafkaConsumer(consumerSetting);
                register(KafkaConsumer.class, null, () -> kafkaConsumer, LifeStyle.Singleton);
                register(IMQConsumer.class, ConsumeKafkaService.class);
            } else {

                Consumer consumer = mqFactory.createPushConsumer(consumerSetting);
                registerInstance(Consumer.class, consumer);
                register(IMQConsumer.class, RocketMQConsumer.class);
            }

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
            if (mqType == TYPE_KAFKA) {
                KafkaProducer kafkaProducer = new KafkaProducer(producerSetting);
                register(KafkaProducer.class, null, () -> kafkaProducer, LifeStyle.Singleton);
                register(IMQProducer.class, SendKafkaService.class);
            } else {
                Producer producer = mqFactory.createProducer(producerSetting);
                registerInstance(Producer.class, producer);
                register(IMQProducer.class, SendRocketMQService.class);
            }

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

    private ENode scanAssemblyTypes() {
        String[] scans = new String[scanPackages.length + ENODE_PACKAGE_SCAN.length];
        System.arraycopy(scanPackages, 0, scans, 0, scanPackages.length);
        System.arraycopy(ENODE_PACKAGE_SCAN, 0, scans, scanPackages.length, ENODE_PACKAGE_SCAN.length);

        FilterBuilder fb = new FilterBuilder();
        fb.include(FilterBuilder.prefix("com.enode.domain.AggregateRoot"));
        fb.include(FilterBuilder.prefix("com.enode.rocketmq.AbstractTopicProvider"));
        fb.include(FilterBuilder.prefix("com.enode.infrastructure.impl.AbstractDenormalizer"));
        fb.include(FilterBuilder.prefix("com.enode.infrastructure.impl.AbstractAsyncDenormalizer"));

        Arrays.stream(scanPackages).forEach(pkg -> fb.include(FilterBuilder.prefix(pkg)));

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(scans)
                        .filterInputsBy(fb)
                        .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner()));
        assemblyTypes = reflections.getSubTypesOf(Object.class);
        return this;
    }

    public ENode start() {
        commitRegisters();
        startENodeComponents();
        initializeBusinessAssemblies();
        //TODO kafka启动
        startMQComponents();
        ENodeJMXAgent.startAgent();
        logger.info("ENode started.");
        return this;
    }

    private void startENodeComponents() {
        resolve(IMemoryCache.class).start();
        resolve(ICommandProcessor.class).start();
        resolve(IEventService.class).start();
        resolve(new GenericTypeLiteral<IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage>>() {
        }).start();
        resolve(new GenericTypeLiteral<IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage>>() {
        }).start();
        resolve(new GenericTypeLiteral<IMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException>>() {
        }).start();
    }

    private void stopENodeComponents() {
        resolve(IMemoryCache.class).stop();
        resolve(ICommandProcessor.class).stop();
        resolve(IEventService.class).stop();

        resolve(new GenericTypeLiteral<IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage>>() {
        }).stop();
        resolve(new GenericTypeLiteral<IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage>>() {
        }).stop();
        resolve(new GenericTypeLiteral<IMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException>>() {
        }).stop();
    }

    public void shutdown() {
        stopENodeComponents();
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

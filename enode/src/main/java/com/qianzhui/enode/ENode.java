package com.qianzhui.enode;

import com.qianzhui.enode.commanding.ICommand;
import com.qianzhui.enode.commanding.ICommandAsyncHandler;
import com.qianzhui.enode.commanding.ICommandAsyncHandlerProvider;
import com.qianzhui.enode.commanding.ICommandHandler;
import com.qianzhui.enode.commanding.ICommandHandlerProvider;
import com.qianzhui.enode.commanding.ICommandProcessor;
import com.qianzhui.enode.commanding.ICommandRoutingKeyProvider;
import com.qianzhui.enode.commanding.ICommandService;
import com.qianzhui.enode.commanding.IProcessingCommandHandler;
import com.qianzhui.enode.commanding.impl.DefaultCommandAsyncHandlerProvider;
import com.qianzhui.enode.commanding.impl.DefaultCommandHandlerProvider;
import com.qianzhui.enode.commanding.impl.DefaultCommandProcessor;
import com.qianzhui.enode.commanding.impl.DefaultCommandRoutingKeyProvider;
import com.qianzhui.enode.commanding.impl.DefaultProcessingCommandHandler;
import com.qianzhui.enode.commanding.impl.NotImplementedCommandService;
import com.qianzhui.enode.common.container.AbstractContainer;
import com.qianzhui.enode.common.container.GenericTypeLiteral;
import com.qianzhui.enode.common.container.IObjectContainer;
import com.qianzhui.enode.common.container.LifeStyle;
import com.qianzhui.enode.common.io.IOHelper;
import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enode.common.scheduling.IScheduleService;
import com.qianzhui.enode.common.scheduling.ScheduleService;
import com.qianzhui.enode.common.serializing.IJsonSerializer;
import com.qianzhui.enode.common.thirdparty.gson.GsonJsonSerializer;
import com.qianzhui.enode.common.thirdparty.guice.GuiceObjectContainer;
import com.qianzhui.enode.configurations.ConfigurationSetting;
import com.qianzhui.enode.configurations.OptionSetting;
import com.qianzhui.enode.domain.AggregateRoot;
import com.qianzhui.enode.domain.IAggregateRepository;
import com.qianzhui.enode.domain.IAggregateRepositoryProvider;
import com.qianzhui.enode.domain.IAggregateRootFactory;
import com.qianzhui.enode.domain.IAggregateRootInternalHandlerProvider;
import com.qianzhui.enode.domain.IAggregateSnapshotter;
import com.qianzhui.enode.domain.IAggregateStorage;
import com.qianzhui.enode.domain.IMemoryCache;
import com.qianzhui.enode.domain.IRepository;
import com.qianzhui.enode.domain.impl.DefaultAggregateRepositoryProvider;
import com.qianzhui.enode.domain.impl.DefaultAggregateRootFactory;
import com.qianzhui.enode.domain.impl.DefaultAggregateRootInternalHandlerProvider;
import com.qianzhui.enode.domain.impl.DefaultAggregateSnapshotter;
import com.qianzhui.enode.domain.impl.DefaultMemoryCache;
import com.qianzhui.enode.domain.impl.DefaultRepository;
import com.qianzhui.enode.domain.impl.EventSourcingAggregateStorage;
import com.qianzhui.enode.domain.impl.SnapshotOnlyAggregateStorage;
import com.qianzhui.enode.eventing.DomainEventStreamMessage;
import com.qianzhui.enode.eventing.IDomainEvent;
import com.qianzhui.enode.eventing.IEventSerializer;
import com.qianzhui.enode.eventing.IEventService;
import com.qianzhui.enode.eventing.IEventStore;
import com.qianzhui.enode.eventing.impl.DefaultEventSerializer;
import com.qianzhui.enode.eventing.impl.DefaultEventService;
import com.qianzhui.enode.eventing.impl.DomainEventStreamMessageHandler;
import com.qianzhui.enode.eventing.impl.InMemoryEventStore;
import com.qianzhui.enode.eventing.impl.MysqlEventStore;
import com.qianzhui.enode.infrastructure.Component;
import com.qianzhui.enode.infrastructure.IApplicationMessage;
import com.qianzhui.enode.infrastructure.IAssemblyInitializer;
import com.qianzhui.enode.infrastructure.ILockService;
import com.qianzhui.enode.infrastructure.IMessageDispatcher;
import com.qianzhui.enode.infrastructure.IMessageHandler;
import com.qianzhui.enode.infrastructure.IMessageHandlerProvider;
import com.qianzhui.enode.infrastructure.IMessageProcessor;
import com.qianzhui.enode.infrastructure.IMessagePublisher;
import com.qianzhui.enode.infrastructure.IProcessingMessageHandler;
import com.qianzhui.enode.infrastructure.IProcessingMessageScheduler;
import com.qianzhui.enode.infrastructure.IPublishableException;
import com.qianzhui.enode.infrastructure.IPublishedVersionStore;
import com.qianzhui.enode.infrastructure.IThreeMessageHandlerProvider;
import com.qianzhui.enode.infrastructure.ITimeProvider;
import com.qianzhui.enode.infrastructure.ITwoMessageHandlerProvider;
import com.qianzhui.enode.infrastructure.ITypeNameProvider;
import com.qianzhui.enode.infrastructure.ProcessingApplicationMessage;
import com.qianzhui.enode.infrastructure.ProcessingDomainEventStreamMessage;
import com.qianzhui.enode.infrastructure.ProcessingPublishableExceptionMessage;
import com.qianzhui.enode.infrastructure.TypeUtils;
import com.qianzhui.enode.infrastructure.impl.DefaultApplicationMessageProcessor;
import com.qianzhui.enode.infrastructure.impl.DefaultDomainEventProcessor;
import com.qianzhui.enode.infrastructure.impl.DefaultMessageDispatcher;
import com.qianzhui.enode.infrastructure.impl.DefaultMessageHandlerProvider;
import com.qianzhui.enode.infrastructure.impl.DefaultProcessingMessageHandler;
import com.qianzhui.enode.infrastructure.impl.DefaultProcessingMessageScheduler;
import com.qianzhui.enode.infrastructure.impl.DefaultPublishableExceptionProcessor;
import com.qianzhui.enode.infrastructure.impl.DefaultThreeMessageHandlerProvider;
import com.qianzhui.enode.infrastructure.impl.DefaultTimeProvider;
import com.qianzhui.enode.infrastructure.impl.DefaultTwoMessageHandlerProvider;
import com.qianzhui.enode.infrastructure.impl.DefaultTypeNameProvider;
import com.qianzhui.enode.infrastructure.impl.DoNothingPublisher;
import com.qianzhui.enode.infrastructure.impl.inmemory.InMemoryPublishedVersionStore;
import com.qianzhui.enode.infrastructure.impl.mysql.MysqlLockService;
import com.qianzhui.enode.infrastructure.impl.mysql.MysqlPublishedVersionStore;
import com.qianzhui.enode.jmx.ENodeJMXAgent;
import com.qianzhui.enode.rocketmq.ITopicProvider;
import com.qianzhui.enode.rocketmq.RocketMQConsumer;
import com.qianzhui.enode.rocketmq.SendRocketMQService;
import com.qianzhui.enode.message.SendReplyService;
import com.qianzhui.enode.rocketmq.TopicTagData;
import com.qianzhui.enode.rocketmq.applicationmessage.ApplicationMessageConsumer;
import com.qianzhui.enode.rocketmq.applicationmessage.ApplicationMessagePublisher;
import com.qianzhui.enode.rocketmq.client.Consumer;
import com.qianzhui.enode.rocketmq.client.Producer;
import com.qianzhui.enode.rocketmq.client.RocketMQFactory;
import com.qianzhui.enode.rocketmq.client.impl.NativeMQFactory;
import com.qianzhui.enode.rocketmq.client.ons.ONSFactory;
import com.qianzhui.enode.rocketmq.command.CommandConsumer;
import com.qianzhui.enode.message.CommandResultProcessor;
import com.qianzhui.enode.rocketmq.command.CommandService;
import com.qianzhui.enode.rocketmq.domainevent.DomainEventConsumer;
import com.qianzhui.enode.rocketmq.domainevent.DomainEventPublisher;
import com.qianzhui.enode.rocketmq.publishableexceptions.PublishableExceptionConsumer;
import com.qianzhui.enode.rocketmq.publishableexceptions.PublishableExceptionPublisher;
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

    private static final Logger logger = ENodeLogger.getLog();

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
    public static final int DOMAIN = DOMAIN_EVENT_PUBLISHER | APPLICATION_MESSAGE_PUBLISHER | EXCEPTION_PUBLISHER | COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER | APPLICATION_MESSAGE_CONSUMER | EXCEPTION_CONSUMER;
    public static final int PUBLISHERS = COMMAND_SERVICE | DOMAIN_EVENT_PUBLISHER | APPLICATION_MESSAGE_PUBLISHER | EXCEPTION_PUBLISHER;
    public static final int CONSUMERS = COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER | APPLICATION_MESSAGE_CONSUMER | EXCEPTION_CONSUMER;
    public static final int ALL_COMPONENTS = PUBLISHERS | CONSUMERS;

    private static final String[] ENODE_PACKAGE_SCAN = new String[]{"com.qianzhui.enode.domain",
            "com.qianzhui.enode.rocketmq",
            "com.qianzhui.enode.infrastructure.impl" //加载AbstractDenormalizer
    };

    private List<Class<?>> _assemblyInitializerServiceTypes;
    private String[] scanPackages;
    private Set<Class<?>> assemblyTypes;
    private ConfigurationSetting setting;
    private int registerRocketMQComponentsFlag;

    private static ENode instance;

    public static ENode getInstance() {
        return instance;
    }

    private ENode(ConfigurationSetting setting, String... packages) {
        this.setting = setting == null ? new ConfigurationSetting() : setting;
        this.scanPackages = packages;
        _assemblyInitializerServiceTypes = new ArrayList<>();

        scanAssemblyTypes();
    }

    public static ENode create(String... packages) {
        return create(null, packages);
    }

    public static ENode create(ConfigurationSetting setting, String... packages) {
        instance = new ENode(setting, packages);
        return instance;
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

    public ENode useMysqlComponents(DataSource ds) {
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

    private static LifeStyle parseComponentLife(Class type) {
        Component annotation = (Component) type.getAnnotation(Component.class);

        if (annotation != null) {
            return annotation.life();
        }

        return LifeStyle.Singleton;
    }

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

    public ENode useONS(Properties producerSetting,
                        Properties consumerSetting,
                        int listenPort,
                        int registerRocketMQComponentsFlag) {
        return useRocketMQ(producerSetting, consumerSetting, registerRocketMQComponentsFlag, listenPort, true);
    }

    public ENode useNativeRocketMQ(Properties producerSetting,
                                   Properties consumerSetting,
                                   int listenPort,
                                   int registerRocketMQComponentsFlag) {
        return useRocketMQ(producerSetting, consumerSetting, registerRocketMQComponentsFlag, listenPort, false);
    }

    private ENode useRocketMQ(Properties producerSetting,
                              Properties consumerSetting,
                              int registerRocketMQComponentsFlag,
                              int listenPort,
                              boolean isONS) {

        this.registerRocketMQComponentsFlag = registerRocketMQComponentsFlag;

        RocketMQFactory mqFactory = isONS ? new ONSFactory() : new NativeMQFactory();

        //Create MQConsumer and any register consumers(CommandConsumer、DomainEventConsumer、ApplicationMessageConsumer、PublishableExceptionConsumer)
        if (hasAnyComponents(registerRocketMQComponentsFlag, CONSUMERS)) {
            Consumer consumer = mqFactory.createPushConsumer(consumerSetting);

            registerInstance(Consumer.class, consumer);
            register(RocketMQConsumer.class);

            // CommandConsumer、DomainEventConsumer需要引用SendReplyService
            if (hasAnyComponents(registerRocketMQComponentsFlag, COMMAND_CONSUMER | DOMAIN_EVENT_CONSUMER)) {
                register(SendReplyService.class);
            }

            //CommandConsumer
            if (hasComponent(registerRocketMQComponentsFlag, COMMAND_CONSUMER)) {
                register(CommandConsumer.class);
            }

            //DomainEventConsumer
            if (hasComponent(registerRocketMQComponentsFlag, DOMAIN_EVENT_CONSUMER)) {
                register(DomainEventConsumer.class);
            }

            //ApplicationMessageConsumer
            if (hasComponent(registerRocketMQComponentsFlag, APPLICATION_MESSAGE_CONSUMER)) {
                register(ApplicationMessageConsumer.class);
            }

            //PublishableExceptionConsumer
            if (hasComponent(registerRocketMQComponentsFlag, EXCEPTION_CONSUMER)) {
                register(PublishableExceptionConsumer.class);
            }
        }

        //Create MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerRocketMQComponentsFlag, PUBLISHERS)) {
            //Create MQProducer
            Producer producer = mqFactory.createProducer(producerSetting);
            registerInstance(Producer.class, producer);

            register(SendRocketMQService.class);

            //CommandService
            if (hasComponent(registerRocketMQComponentsFlag, COMMAND_SERVICE)) {
                register(CommandResultProcessor.class, null, () -> {
                    IJsonSerializer jsonSerializer = resolve(IJsonSerializer.class);
                    return new CommandResultProcessor(listenPort, jsonSerializer);
                }, LifeStyle.Singleton);
                register(ICommandService.class, CommandService.class);
            }

            //DomainEventPublisher
            if (hasComponent(registerRocketMQComponentsFlag, DOMAIN_EVENT_PUBLISHER)) {
                register(new GenericTypeLiteral<IMessagePublisher<DomainEventStreamMessage>>() {
                }, DomainEventPublisher.class);
            }

            //ApplicationMessagePublisher
            if (hasComponent(registerRocketMQComponentsFlag, APPLICATION_MESSAGE_PUBLISHER)) {
                register(new GenericTypeLiteral<IMessagePublisher<IApplicationMessage>>() {
                }, ApplicationMessagePublisher.class);
            }

            //PublishableExceptionPublisher
            if (hasComponent(registerRocketMQComponentsFlag, EXCEPTION_PUBLISHER)) {
                register(new GenericTypeLiteral<IMessagePublisher<IPublishableException>>() {
                }, PublishableExceptionPublisher.class);
            }
        }

        return this;
    }

    private void startRocketMQComponents() {
        //Start MQConsumer and any register consumers(CommandConsumer、DomainEventConsumer、ApplicationMessageConsumer、PublishableExceptionConsumer)
        if (hasAnyComponents(registerRocketMQComponentsFlag, CONSUMERS)) {
            //All topic
            Set<TopicTagData> topicTagDatas = new HashSet<>();

            //CommandConsumer
            if (hasComponent(registerRocketMQComponentsFlag, COMMAND_CONSUMER)) {
                CommandConsumer commandConsumer = resolve(CommandConsumer.class);
                commandConsumer.start();

                //Command topics
                ITopicProvider<ICommand> commandTopicProvider = resolve(new GenericTypeLiteral<ITopicProvider<ICommand>>() {
                });
                topicTagDatas.addAll(commandTopicProvider.getAllSubscribeTopics());
            }

            //DomainEventConsumer
            if (hasComponent(registerRocketMQComponentsFlag, DOMAIN_EVENT_CONSUMER)) {
                DomainEventConsumer domainEventConsumer = resolve(DomainEventConsumer.class);
                domainEventConsumer.start();

                //Domain event topics
                ITopicProvider<IDomainEvent> domainEventTopicProvider = resolve(new GenericTypeLiteral<ITopicProvider<IDomainEvent>>() {
                });
                topicTagDatas.addAll(domainEventTopicProvider.getAllSubscribeTopics());
            }

            //ApplicationMessageConsumer
            if (hasComponent(registerRocketMQComponentsFlag, APPLICATION_MESSAGE_CONSUMER)) {
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
            if (hasComponent(registerRocketMQComponentsFlag, EXCEPTION_CONSUMER)) {
                PublishableExceptionConsumer publishableExceptionConsumer = resolve(PublishableExceptionConsumer.class);
                publishableExceptionConsumer.start();

                //Exception topics
                ITopicProvider<IPublishableException> exceptionTopicProvider = resolve(new GenericTypeLiteral<ITopicProvider<IPublishableException>>() {
                });
                if (exceptionTopicProvider != null) {
                    topicTagDatas.addAll(exceptionTopicProvider.getAllSubscribeTopics());
                }
            }

            RocketMQConsumer rocketMQConsumer = resolve(RocketMQConsumer.class);
            topicTagDatas.stream().collect(Collectors.groupingBy(TopicTagData::getTopic)).forEach((topic, tags) -> {
                String tagsJoin = tags.stream().map(TopicTagData::getTag).collect(Collectors.joining("||"));
                rocketMQConsumer.subscribe(topic, tagsJoin);
            });

            rocketMQConsumer.start();
        }

        //Start MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerRocketMQComponentsFlag, PUBLISHERS)) {
            //Start MQProducer
            Producer producer = resolve(Producer.class);
            producer.start();

            //CommandService
            if (hasComponent(registerRocketMQComponentsFlag, COMMAND_SERVICE)) {
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
        fb.include(FilterBuilder.prefix("com.qianzhui.enode.domain.AggregateRoot"));
        fb.include(FilterBuilder.prefix("com.qianzhui.enode.rocketmq.AbstractTopicProvider"));
        fb.include(FilterBuilder.prefix("com.qianzhui.enode.infrastructure.impl.AbstractDenormalizer"));
        fb.include(FilterBuilder.prefix("com.qianzhui.enode.infrastructure.impl.AbstractAsyncDenormalizer"));

        Arrays.stream(scanPackages).forEach(pkg -> fb.include(FilterBuilder.prefix(pkg)));

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(scans)
                        .filterInputsBy(fb)
                        .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner()));

        assemblyTypes = reflections.getSubTypesOf(Object.class);

        return this;
    }

    private static boolean isAssemblyInitializer(Class type) {
        return !Modifier.isAbstract(type.getModifiers()) && IAssemblyInitializer.class.isAssignableFrom(type);
    }

    public ENode start() {
        commitRegisters();
        startENodeComponents();
        initializeBusinessAssemblies();
        startRocketMQComponents();
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
        if (hasAnyComponents(registerRocketMQComponentsFlag, CONSUMERS)) {
            //CommandConsumer
            if (hasComponent(registerRocketMQComponentsFlag, COMMAND_CONSUMER)) {
                CommandConsumer commandConsumer = resolve(CommandConsumer.class);
                commandConsumer.shutdown();
            }

            //DomainEventConsumer
            if (hasComponent(registerRocketMQComponentsFlag, DOMAIN_EVENT_CONSUMER)) {
                DomainEventConsumer domainEventConsumer = resolve(DomainEventConsumer.class);
                domainEventConsumer.shutdown();
            }

            //ApplicationMessageConsumer
            if (hasComponent(registerRocketMQComponentsFlag, APPLICATION_MESSAGE_CONSUMER)) {
                ApplicationMessageConsumer applicationMessageConsumer = resolve(ApplicationMessageConsumer.class);
                applicationMessageConsumer.shutdown();
            }

            //PublishableExceptionConsumer
            if (hasComponent(registerRocketMQComponentsFlag, EXCEPTION_CONSUMER)) {
                PublishableExceptionConsumer publishableExceptionConsumer = resolve(PublishableExceptionConsumer.class);
                publishableExceptionConsumer.shutdown();
            }

            RocketMQConsumer consumer = resolve(RocketMQConsumer.class);
            consumer.shutdown();
        }

        //Shutdown MQProducer and any register publishers(CommandService、DomainEventPublisher、ApplicationMessagePublisher、PublishableExceptionPublisher)
        if (hasAnyComponents(registerRocketMQComponentsFlag, PUBLISHERS)) {
            //Start MQProducer
            Producer producer = resolve(Producer.class);
            producer.shutdown();

            //CommandService
            if (hasComponent(registerRocketMQComponentsFlag, COMMAND_SERVICE)) {
                CommandService commandService = resolve(CommandService.class);
                commandService.shutdown();
            }
        }
    }
}

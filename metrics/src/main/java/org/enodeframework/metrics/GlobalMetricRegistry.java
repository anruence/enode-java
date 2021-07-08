package org.enodeframework.metrics;

import com.codahale.metrics.MetricRegistry;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.extensions.MessageMonitor;
import org.enodeframework.common.extensions.NoOpMessageMonitor;
import org.enodeframework.eventing.DomainEvent;
import org.enodeframework.eventing.IDomainEvent;
import org.enodeframework.eventing.IProcessingEventProcessor;
import org.enodeframework.messaging.IMessage;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Registry for application metrics with convenient ways
 */
public class GlobalMetricRegistry {

    private static final Logger logger = LoggerFactory.getLogger(GlobalMetricRegistry.class);

    private final MetricRegistry registry;

    /**
     * Initializes a new {@link GlobalMetricRegistry} delegating to a new {@link MetricRegistry} with default settings.
     */
    public GlobalMetricRegistry() {
        this(new MetricRegistry());
    }

    /**
     * Initializes a {@link GlobalMetricRegistry} delegating to the given {@code metricRegistry}.
     *
     * @param metricRegistry the {@link MetricRegistry} which will record the metrics
     */
    public GlobalMetricRegistry(MetricRegistry metricRegistry) {
        this.registry = metricRegistry;
    }

    /**
     * Registers the configured {@link MetricRegistry} with the given via
     * Components registered will be added by invocation of {@link #registerComponent(Class, String)}.
     */
    public void registerWithConfigurer(Class<?> componentType, String componentName) {
        registerComponent(componentType, componentName);
    }

    /**
     * Registers new metrics to the {@link MetricRegistry} to monitor a component of given {@code componentType}. The
     * monitor will be registered with the registry under the given {@code componentName}. The returned {@link
     * MessageMonitor} can be installed on the component to initiate the monitoring.
     *
     * @param componentType the type of component to register
     * @param componentName the name under which the component should be registered to the registry
     * @return a {@link MessageMonitor} to monitor the behavior of the given {@code componentType}
     */
    public MessageMonitor<? extends IMessage> registerComponent(Class<?> componentType, String componentName) {
        if (IDomainEvent.class.isAssignableFrom(componentType)) {
            return registerEventProcessor(componentName);
        }
        if (ICommand.class.isAssignableFrom(componentType)) {
            return registerCommandBus(componentName);
        }
        if (IMessage.class.isAssignableFrom(componentType)) {
            return registerEventBus(componentName);
        }
        logger.warn("Cannot provide MessageMonitor for component [{}] of type [{}]. Returning No-Op instance.",
            componentName, componentType.getSimpleName());
        return NoOpMessageMonitor.INSTANCE;
    }

    /**
     * Registers new metrics to the registry to monitor an {@link IProcessingEventProcessor}. The monitor will be registered with
     * the registry under the given {@code eventProcessorName}. The returned {@link MessageMonitor} can be installed on
     * the {@code EventProcessor} to initiate the monitoring.
     *
     * @param eventProcessorName the name under which the {@link IProcessingEventProcessor} should be registered to the registry
     * @return a {@link MessageMonitor} to monitor the behavior of an {@link IProcessingEventProcessor}
     */
    public MessageMonitor<? super DomainEvent<?>> registerEventProcessor(String eventProcessorName) {
        MessageTimerMonitor messageTimerMonitor = MessageTimerMonitor.builder().build();
        EventProcessorLatencyMonitor eventProcessorLatencyMonitor = new EventProcessorLatencyMonitor();
        CapacityMonitor capacityMonitor = new CapacityMonitor(1, TimeUnit.MINUTES);
        MessageCountingMonitor messageCountingMonitor = new MessageCountingMonitor();

        MetricRegistry eventProcessingRegistry = new MetricRegistry();
        eventProcessingRegistry.register("messageTimer", messageTimerMonitor);
        eventProcessingRegistry.register("latency", eventProcessorLatencyMonitor);
        eventProcessingRegistry.register("messageCounter", messageCountingMonitor);
        eventProcessingRegistry.register("capacity", capacityMonitor);
        registry.register(eventProcessorName, eventProcessingRegistry);

        List<MessageMonitor<? super DomainEvent<?>>> monitors = new ArrayList<>();
        monitors.add(messageTimerMonitor);
        monitors.add(eventProcessorLatencyMonitor);
        monitors.add(capacityMonitor);
        monitors.add(messageCountingMonitor);
        return new MultiMessageMonitor<>(monitors);
    }

    /**
     * Registers new metrics to the registry to monitor a {@link ICommandService}. The monitor will be registered with the
     * registry under the given {@code commandBusName}. The returned {@link MessageMonitor} can be installed on the
     * {@code CommandBus} to initiate the monitoring.
     *
     * @param commandBusName the name under which the commandBus should be registered to the registry
     * @return a {@link MessageMonitor} to monitor the behavior of a CommandBus
     */
    public MessageMonitor<? super ICommand> registerCommandBus(String commandBusName) {
        return registerDefaultHandlerMessageMonitor(commandBusName);
    }

    /**
     * Registers new metrics to the registry to monitor an {@link IMessagePublisher}. The monitor will be registered with the
     * registry under the given {@code eventBusName}. The returned {@link MessageMonitor} can be installed on the {@code
     * EventBus} to initiate the monitoring.
     *
     * @param eventBusName the name under which the {@link IMessagePublisher} should be registered to the registry
     * @return a {@link MessageMonitor} to monitor the behavior of an {@link IMessagePublisher}
     */
    public MessageMonitor<? super Message> registerEventBus(String eventBusName) {
        MessageCountingMonitor messageCounterMonitor = new MessageCountingMonitor();
        MessageTimerMonitor messageTimerMonitor = MessageTimerMonitor.builder().build();

        MetricRegistry eventProcessingRegistry = new MetricRegistry();
        eventProcessingRegistry.register("messageCounter", messageCounterMonitor);
        eventProcessingRegistry.register("messageTimer", messageTimerMonitor);
        registry.register(eventBusName, eventProcessingRegistry);

        return new MultiMessageMonitor<>(Arrays.asList(messageCounterMonitor, messageTimerMonitor));
    }

    private MessageMonitor<IMessage> registerDefaultHandlerMessageMonitor(String name) {
        MessageTimerMonitor messageTimerMonitor = MessageTimerMonitor.builder().build();
        CapacityMonitor capacityMonitor = new CapacityMonitor(1, TimeUnit.MINUTES);
        MessageCountingMonitor messageCountingMonitor = new MessageCountingMonitor();

        MetricRegistry handlerRegistry = new MetricRegistry();
        handlerRegistry.register("messageTimer", messageTimerMonitor);
        handlerRegistry.register("capacity", capacityMonitor);
        handlerRegistry.register("messageCounter", messageCountingMonitor);
        registry.register(name, handlerRegistry);

        return new MultiMessageMonitor<>(messageTimerMonitor, capacityMonitor, messageCountingMonitor);
    }

    /**
     * Returns the global {@link MetricRegistry} to which components are registered.
     *
     * @return the global registry
     */
    public MetricRegistry getRegistry() {
        return registry;
    }
}

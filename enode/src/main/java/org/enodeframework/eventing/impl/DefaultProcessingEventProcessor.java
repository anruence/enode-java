package org.enodeframework.eventing.impl;

import com.google.common.base.Strings;
import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.scheduling.IScheduleService;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.IProcessingEventProcessor;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.enodeframework.eventing.ProcessingEvent;
import org.enodeframework.eventing.ProcessingEventMailBox;
import org.enodeframework.messaging.IMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DefaultProcessingEventProcessor implements IProcessingEventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessingEventProcessor.class);
    private final Object lockObj = new Object();
    private int timeoutSeconds = 3600 * 24 * 3;
    private int scanExpiredAggregateIntervalMilliseconds = 5000;
    private String taskName;

    private String processorName = "DefaultEventProcessor";

    private ConcurrentHashMap<String, ProcessingEventMailBox> mailboxDict;

    @Autowired
    private IScheduleService scheduleService;

    @Autowired
    private IMessageDispatcher dispatcher;

    @Autowired
    private IPublishedVersionStore publishedVersionStore;

    public DefaultProcessingEventProcessor() {
        mailboxDict = new ConcurrentHashMap<>();
        taskName = "CleanInactiveProcessingEventMailBoxes_" + System.nanoTime() + new Random().nextInt(10000);
    }


    @Override
    public void process(ProcessingEvent processingEvent) {
        String aggregateRootId = processingEvent.getMessage().getAggregateRootId();
        if (Strings.isNullOrEmpty(aggregateRootId)) {
            throw new IllegalArgumentException("aggregateRootId of domain event stream cannot be null or empty, domainEventStreamId:" + processingEvent.getMessage().getId());
        }
        synchronized (lockObj) {
            ProcessingEventMailBox mailbox = mailboxDict.computeIfAbsent(aggregateRootId, key -> {
                int latestHandledEventVersion = getAggregateRootLatestHandledEventVersion(processingEvent.getMessage().getAggregateRootTypeName(), aggregateRootId);
                return new ProcessingEventMailBox(aggregateRootId, latestHandledEventVersion, y -> dispatchProcessingMessageAsync(y, 0));
            });
            mailbox.enqueueMessage(processingEvent);
        }
    }

    @Override
    public void start() {
        scheduleService.startTask(taskName, this::cleanInactiveMailbox, scanExpiredAggregateIntervalMilliseconds, scanExpiredAggregateIntervalMilliseconds);
    }

    @Override
    public void stop() {
        scheduleService.stopTask(taskName);
    }

    private void dispatchProcessingMessageAsync(ProcessingEvent processingEvent, int retryTimes) {
        IOHelper.tryAsyncActionRecursivelyWithoutResult("DispatchProcessingMessageAsync",
                () -> dispatcher.dispatchMessagesAsync(processingEvent.getMessage().getEvents()),
                result -> {
                    updatePublishedVersionAsync(processingEvent, 0);
                },
                () -> String.format("sequence message [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%s]", processingEvent.getMessage().getId(), processingEvent.getMessage().getClass().getName(), processingEvent.getMessage().getAggregateRootId(), processingEvent.getMessage().getVersion()),
                null,
                retryTimes, true);
    }

    private int getAggregateRootLatestHandledEventVersion(String aggregateRootType, String aggregateRootId) {
        try {
            return Task.await(publishedVersionStore.getPublishedVersionAsync(processorName, aggregateRootType, aggregateRootId));
        } catch (Exception ex) {
            throw new ENodeRuntimeException("_publishedVersionStore.GetPublishedVersionAsync has unknown exception.", ex);
        }
    }

    private void updatePublishedVersionAsync(ProcessingEvent processingEvent, int retryTimes) {
        DomainEventStreamMessage message = processingEvent.getMessage();
        IOHelper.tryAsyncActionRecursivelyWithoutResult("UpdatePublishedVersionAsync",
                () -> publishedVersionStore.updatePublishedVersionAsync(processorName, message.getAggregateRootTypeName(), message.getAggregateRootId(), message.getVersion()),
                result -> {
                    processingEvent.complete();
                },
                () -> String.format("DomainEventStreamMessage [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%s]", message.getId(), message.getClass().getName(), message.getAggregateRootId(), message.getVersion()),
                null, retryTimes, true);
    }


    private void cleanInactiveMailbox() {
        List<Map.Entry<String, ProcessingEventMailBox>> inactiveList = mailboxDict.entrySet().stream()
                .filter(entry -> entry.getValue().isInactive(timeoutSeconds)
                        && !entry.getValue().isRunning()
                        && entry.getValue().getTotalUnHandledMessageCount() == 0)
                .collect(Collectors.toList());
        inactiveList.forEach(entry -> {
            synchronized (lockObj) {
                if (entry.getValue().isInactive(timeoutSeconds)
                        && !entry.getValue().isRunning()
                        && entry.getValue().getTotalUnHandledMessageCount() == 0) {
                    if (mailboxDict.remove(entry.getKey()) != null) {
                        logger.info("Removed inactive domain event stream mailbox, aggregateRootId: {}", entry.getKey());
                    }
                }
            }
        });
    }
}

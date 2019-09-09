package org.enodeframework.eventing.impl;

import com.google.common.base.Strings;
import org.enodeframework.common.exception.ArgumentException;
import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.scheduling.IScheduleService;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.IProcessingDomainEventStreamMessageProcessor;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.enodeframework.eventing.ProcessingDomainEventStreamMessage;
import org.enodeframework.eventing.ProcessingDomainEventStreamMessageMailBox;
import org.enodeframework.messaging.IMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DefaultProcessingDomainEventStreamMessageProcessor implements IProcessingDomainEventStreamMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessingDomainEventStreamMessageProcessor.class);
    private final Object lockObj = new Object();
    private int timeoutSeconds = 3600 * 24 * 3;
    private int scanExpiredAggregateIntervalMilliseconds = 5000;
    private String taskName;

    private String processorName = "DefaultEventProcessor";

    private ConcurrentMap<String, ProcessingDomainEventStreamMessageMailBox> mailboxDict;
    @Autowired
    private IScheduleService scheduleService;

    @Autowired
    private IMessageDispatcher dispatcher;

    @Autowired
    private IPublishedVersionStore publishedVersionStore;

    public DefaultProcessingDomainEventStreamMessageProcessor() {
        mailboxDict = new ConcurrentHashMap<>();
        taskName = "CleanInactiveProcessingDomainEventStreamMessageMailBoxes_" + System.nanoTime() + new Random().nextInt(10000);
    }


    @Override
    public void process(ProcessingDomainEventStreamMessage processingMessage) {
        String aggregateRootId = processingMessage.getMessage().getAggregateRootId();
        if (Strings.isNullOrEmpty(aggregateRootId)) {
            throw new ArgumentException("aggregateRootId of domain event stream cannot be null or empty, domainEventStreamId:" + processingMessage.getMessage().getId());
        }
        synchronized (lockObj) {
            ProcessingDomainEventStreamMessageMailBox mailbox = mailboxDict.computeIfAbsent(aggregateRootId, key -> {
                int latestHandledEventVersion = getAggregateRootLatestHandledEventVersion(processingMessage.getMessage().getAggregateRootTypeName(), aggregateRootId);
                return new ProcessingDomainEventStreamMessageMailBox(aggregateRootId, latestHandledEventVersion, y -> dispatchProcessingMessageAsync(y, 0));
            });
            mailbox.enqueueMessage(processingMessage);
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

    private void dispatchProcessingMessageAsync(ProcessingDomainEventStreamMessage processingMessage, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("DispatchProcessingMessageAsync",
                () -> dispatcher.dispatchMessagesAsync(processingMessage.getMessage().getEvents()),
                result -> {
                    updatePublishedVersionAsync(processingMessage, 0);
                },
                () -> String.format("sequence message [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%s]", processingMessage.getMessage().getId(), processingMessage.getMessage().getClass().getName(), processingMessage.getMessage().getAggregateRootId(), processingMessage.getMessage().getVersion()),
                errorMessage -> {
                    logger.error("Dispatching message has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage);
                },
                retryTimes, true);
    }

    private int getAggregateRootLatestHandledEventVersion(String aggregateRootType, String aggregateRootId) {
        try {
            AsyncTaskResult<Integer> task = Task.await(publishedVersionStore.getPublishedVersionAsync(processorName, aggregateRootType, aggregateRootId));
            if (task.getStatus() == AsyncTaskStatus.Success) {
                return task.getData();
            } else {
                throw new Exception("_publishedVersionStore.GetPublishedVersionAsync has unknown exception, errorMessage: " + task.getErrorMessage());
            }
        } catch (Exception ex) {
            throw new ENodeRuntimeException("_publishedVersionStore.GetPublishedVersionAsync has unknown exception.", ex);
        }
    }

    private void updatePublishedVersionAsync(ProcessingDomainEventStreamMessage processingMessage, int retryTimes) {
        DomainEventStreamMessage message = processingMessage.getMessage();
        IOHelper.tryAsyncActionRecursively("UpdatePublishedVersionAsync",
                () -> publishedVersionStore.updatePublishedVersionAsync(processorName, message.getAggregateRootTypeName(), message.getAggregateRootId(), message.getVersion()),
                result -> {
                    processingMessage.complete();
                },
                () -> String.format("DomainEventStreamMessage [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%s]", message.getId(), message.getClass().getName(), message.getAggregateRootId(), message.getVersion()),
                errorMessage -> {
                    logger.error("Update published version has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage);
                }, retryTimes, true);
    }


    private void cleanInactiveMailbox() {
        List<Map.Entry<String, ProcessingDomainEventStreamMessageMailBox>> inactiveList = mailboxDict.entrySet().stream()
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

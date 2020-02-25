package org.enodeframework.commanding.impl;

import com.google.common.base.Strings;
import org.enodeframework.commanding.ICommandProcessor;
import org.enodeframework.commanding.IProcessingCommandHandler;
import org.enodeframework.commanding.ProcessingCommand;
import org.enodeframework.commanding.ProcessingCommandMailbox;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.scheduling.IScheduleService;
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
public class DefaultCommandProcessor implements ICommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCommandProcessor.class);
    private final ConcurrentMap<String, ProcessingCommandMailbox> mailboxDict;
    private final Object lockObj = new Object();
    private int timeoutSeconds;
    private String taskName;
    private int commandMailBoxPersistenceMaxBatchSize = 1000;
    private int scanExpiredAggregateIntervalMilliseconds = 5000;
    private int aggregateRootMaxInactiveSeconds = 3600 * 24 * 3;

    @Autowired
    private IProcessingCommandHandler processingCommandHandler;
    @Autowired
    private IScheduleService scheduleService;

    public DefaultCommandProcessor() {
        this.mailboxDict = new ConcurrentHashMap<>();
        this.timeoutSeconds = aggregateRootMaxInactiveSeconds;
        this.taskName = "CleanInactiveProcessingCommandMailBoxes_" + System.nanoTime() + new Random().nextInt(10000);
    }

    public DefaultCommandProcessor setCommandMailBoxPersistenceMaxBatchSize(int commandMailBoxPersistenceMaxBatchSize) {
        this.commandMailBoxPersistenceMaxBatchSize = commandMailBoxPersistenceMaxBatchSize;
        return this;
    }

    public DefaultCommandProcessor setScanExpiredAggregateIntervalMilliseconds(int scanExpiredAggregateIntervalMilliseconds) {
        this.scanExpiredAggregateIntervalMilliseconds = scanExpiredAggregateIntervalMilliseconds;
        return this;
    }

    public DefaultCommandProcessor setAggregateRootMaxInactiveSeconds(int aggregateRootMaxInactiveSeconds) {
        this.aggregateRootMaxInactiveSeconds = aggregateRootMaxInactiveSeconds;
        return this;
    }

    public DefaultCommandProcessor setProcessingCommandHandler(IProcessingCommandHandler processingCommandHandler) {
        this.processingCommandHandler = processingCommandHandler;
        return this;
    }

    public DefaultCommandProcessor setScheduleService(IScheduleService scheduleService) {
        this.scheduleService = scheduleService;
        return this;
    }

    @Override
    public void process(ProcessingCommand processingCommand) {
        String aggregateRootId = processingCommand.getMessage().getAggregateRootId();
        if (Strings.isNullOrEmpty(aggregateRootId)) {
            throw new IllegalArgumentException("aggregateRootId of command cannot be null or empty, commandId:" + processingCommand.getMessage().getId());
        }
        ProcessingCommandMailbox mailbox = mailboxDict.computeIfAbsent(aggregateRootId, x -> new ProcessingCommandMailbox(x, processingCommandHandler, commandMailBoxPersistenceMaxBatchSize));
        mailbox.enqueueMessage(processingCommand);

        long mailboxTryUsingCount = 0L;
        while (!mailbox.tryUsing()) {
            Task.sleep(1);
            mailboxTryUsingCount++;
            if (mailboxTryUsingCount % 10000 == 0) {
                logger.warn("Command mailbox try using count: {}, aggregateRootId: {}", mailboxTryUsingCount, mailbox.getAggregateRootId());
            }
        }
        if (mailbox.isRemoved()) {
            mailbox = new ProcessingCommandMailbox(aggregateRootId, processingCommandHandler, commandMailBoxPersistenceMaxBatchSize);
            mailboxDict.putIfAbsent(aggregateRootId, mailbox);
        }
        mailbox.enqueueMessage(processingCommand);
        mailbox.exitUsing();
    }

    @Override
    public void start() {
        scheduleService.startTask(taskName, this::cleanInactiveMailbox,
                scanExpiredAggregateIntervalMilliseconds,
                scanExpiredAggregateIntervalMilliseconds);
    }

    @Override
    public void stop() {
        scheduleService.stopTask(taskName);
    }

    private boolean isMailBoxAllowRemove(ProcessingCommandMailbox mailbox) {
        return mailbox.isInactive(timeoutSeconds) && !mailbox.isRunning() && mailbox.getTotalUnHandledMessageCount() == 0;
    }

    private void cleanInactiveMailbox() {
        List<Map.Entry<String, ProcessingCommandMailbox>> inactiveList = mailboxDict.entrySet().stream()
                .filter(entry -> isMailBoxAllowRemove(entry.getValue()))
                .collect(Collectors.toList());
        inactiveList.forEach(entry -> {
            if (isMailBoxAllowRemove(entry.getValue())) {
                ProcessingCommandMailbox removed = mailboxDict.remove(entry.getKey());
                if (removed != null) {
                    removed.markAsRemoved();
                    logger.info("Removed inactive command mailbox, aggregateRootId: {}", entry.getKey());
                }
            }
        });
    }
}

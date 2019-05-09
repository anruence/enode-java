package com.enode.commanding.impl;

import com.enode.commanding.ICommandProcessor;
import com.enode.commanding.IProcessingCommandHandler;
import com.enode.commanding.ProcessingCommand;
import com.enode.commanding.ProcessingCommandMailbox;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.scheduling.IScheduleService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class DefaultCommandProcessor implements ICommandProcessor {
    private static final Logger _logger = ENodeLogger.getLog();

    private final ConcurrentMap<String, ProcessingCommandMailbox> _mailboxDict;
    private final int _timeoutSeconds;
    private final String _taskName;
    private final int commandMailBoxPersistenceMaxBatchSize = 1000;
    private final int scanExpiredAggregateIntervalMilliseconds = 5000;
    private final int eventMailBoxPersistenceMaxBatchSize = 1000;
    private final int aggregateRootMaxInactiveSeconds = 3600 * 24 * 3;
    @Autowired
    private IProcessingCommandHandler _handler;
    @Autowired
    private IScheduleService _scheduleService;

    public DefaultCommandProcessor() {
        _mailboxDict = new ConcurrentHashMap<>();
        _timeoutSeconds = aggregateRootMaxInactiveSeconds;
        _taskName = "CleanInactiveAggregates_" + System.nanoTime() + new Random().nextInt(10000);
    }

    @Override
    public void process(ProcessingCommand processingCommand) {
        String aggregateRootId = processingCommand.getMessage().getAggregateRootId();
        if (aggregateRootId == null || "".equals(aggregateRootId.trim())) {
            throw new IllegalArgumentException("aggregateRootId of command cannot be null or empty, commandId:" + processingCommand.getMessage().id());
        }

        ProcessingCommandMailbox mailbox = _mailboxDict.computeIfAbsent(aggregateRootId, x -> new ProcessingCommandMailbox(x, _handler));
        mailbox.enqueueMessage(processingCommand);
    }

    @Override
    public void start() {
        _scheduleService.startTask(_taskName, this::cleanInactiveMailbox,
                scanExpiredAggregateIntervalMilliseconds,
                scanExpiredAggregateIntervalMilliseconds);
    }

    @Override
    public void stop() {
        _scheduleService.stopTask(_taskName);
    }

    private void cleanInactiveMailbox() {
        List<Map.Entry<String, ProcessingCommandMailbox>> inactiveList = _mailboxDict.entrySet().stream().filter(entry ->
                entry.getValue().isInactive(_timeoutSeconds) && !entry.getValue().isRunning()
        ).collect(Collectors.toList());

        inactiveList.forEach(entry -> {
            if (_mailboxDict.remove(entry.getKey()) != null) {
                _logger.info("Removed inactive command mailbox, aggregateRootId: {}", entry.getKey());
            }
        });
    }
}

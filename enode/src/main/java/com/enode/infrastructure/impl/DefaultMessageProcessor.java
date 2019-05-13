package com.enode.infrastructure.impl;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.scheduling.IScheduleService;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.IProcessingMessage;
import com.enode.infrastructure.IProcessingMessageHandler;
import com.enode.infrastructure.IProcessingMessageScheduler;
import com.enode.infrastructure.ProcessingMessageMailbox;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class DefaultMessageProcessor<X extends IProcessingMessage<X, Y>, Y extends IMessage> implements IMessageProcessor<X, Y> {

    private static final Logger _logger = ENodeLogger.getLog();

    // AggregateRootMaxInactiveSeconds = 3600 * 24 * 3;

    private final int _timeoutSeconds = 3600 * 24 * 3;

    private final int _scanExpiredAggregateIntervalMilliseconds = 5000;

    private final String _taskName;

    private ConcurrentMap<String, ProcessingMessageMailbox<X, Y>> _mailboxDict;

    @Autowired
    private IScheduleService _scheduleService;

    @Autowired
    private IProcessingMessageScheduler<X, Y> _processingMessageScheduler;

    @Autowired
    private IProcessingMessageHandler<X, Y> _processingMessageHandler;

    public DefaultMessageProcessor() {
        _mailboxDict = new ConcurrentHashMap<>();
        _taskName = "CleanInactiveAggregates_" + System.nanoTime() + new Random().nextInt(10000);
    }

    public String getMessageName() {
        return "message";
    }

    @Override
    public void process(X processingMessage) {
        String routingKey = processingMessage.getMessage().getRoutingKey();
        if (routingKey != null && !"".equals(routingKey.trim())) {
            ProcessingMessageMailbox<X, Y> mailbox = _mailboxDict.computeIfAbsent(routingKey, key -> new ProcessingMessageMailbox<>(routingKey, _processingMessageScheduler, _processingMessageHandler));
            mailbox.enqueueMessage(processingMessage);
        } else {
            _processingMessageScheduler.scheduleMessage(processingMessage);
        }
    }

    @Override
    public void start() {
        _scheduleService.startTask(_taskName, this::cleanInactiveMailbox, _scanExpiredAggregateIntervalMilliseconds, _scanExpiredAggregateIntervalMilliseconds);
    }

    @Override
    public void stop() {
        _scheduleService.stopTask(_taskName);
    }

    private void cleanInactiveMailbox() {
        List<Map.Entry<String, ProcessingMessageMailbox<X, Y>>> inactiveList = _mailboxDict.entrySet().stream().filter(entry ->
                entry.getValue().isInactive(_timeoutSeconds) && !entry.getValue().isRunning()
        ).collect(Collectors.toList());

        inactiveList.forEach(entry -> {
            if (_mailboxDict.remove(entry.getKey()) != null) {
                _logger.info("Removed inactive {} mailbox, aggregateRootId: {}", getMessageName(), entry.getKey());
            }
        });
    }
}

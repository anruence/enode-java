package org.enodeframework.eventing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.enodeframework.common.exception.MailBoxProcessException
import org.enodeframework.common.function.Action1
import org.enodeframework.common.io.Task
import org.enodeframework.common.utilities.SystemClock
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

class ProcessingEventMailBox(val aggregateRootTypeName: String, val aggregateRootId: String, private var handleProcessingEventAction: Action1<ProcessingEvent>) {
    private val lockObj = Any()
    private val isUsing = AtomicInteger(0)
    private val isRemoved = AtomicInteger(0)
    private val isRunning = AtomicInteger(0)
    private var waitingProcessingEventDict = ConcurrentHashMap<Int, ProcessingEvent>()
    private var processingEventQueue: ConcurrentLinkedQueue<ProcessingEvent> = ConcurrentLinkedQueue()
    private var lastActiveTime: Date
    private var nextExpectingEventVersion: Int? = null

    private fun tryRemovedInvalidWaitingMessages(version: Int) {
        waitingProcessingEventDict.keys.stream().filter { x: Int -> x < version }.forEach { key: Int ->
            if (waitingProcessingEventDict.containsKey(key)) {
                val processingEvent = waitingProcessingEventDict.remove(key)
                processingEvent!!.complete()
                logger.warn("{} invalid waiting message removed, aggregateRootType: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventTypes: {}, eventIds: {}, nextExpectingEventVersion: {}",
                        javaClass.name,
                        processingEvent.message.getAggregateRootTypeName(),
                        processingEvent.message.getAggregateRootId(),
                        processingEvent.message.commandId,
                        processingEvent.message.getVersion(),
                        processingEvent.message.id,
                        processingEvent.message.events.stream().map { x: IDomainEvent<*> -> x.javaClass.name }.collect(Collectors.joining("|")),
                        processingEvent.message.events.stream().map { obj: IDomainEvent<*> -> obj.id }.collect(Collectors.joining("|")),
                        version)
            }
        }
    }

    private fun tryEnqueueValidWaitingMessage() {
        if (nextExpectingEventVersion == null) {
            return
        }
        while (waitingProcessingEventDict.containsKey(nextExpectingEventVersion)) {
            val nextProcessingEvent = waitingProcessingEventDict.remove(nextExpectingEventVersion)
            if (nextProcessingEvent != null) {
                enqueueEventStream(nextProcessingEvent)
                logger.info("{} enqueued waiting processingEvent, aggregateRootId: {}, aggregateRootTypeName: {}, eventVersion: {}", javaClass.name, aggregateRootId, aggregateRootTypeName, nextProcessingEvent.message.getVersion())
            }
        }
    }

    fun getTotalUnHandledMessageCount(): Int {
        return processingEventQueue.size
    }

    fun setNextExpectingEventVersion(version: Int) {
        synchronized(lockObj) {
            tryRemovedInvalidWaitingMessages(version)
            if (nextExpectingEventVersion == null || version > this.nextExpectingEventVersion!!) {
                nextExpectingEventVersion = version
                logger.info("{} refreshed nextExpectingEventVersion, aggregateRootId: {}, aggregateRootTypeName: {}, version: {}", javaClass.name, aggregateRootId, aggregateRootTypeName, nextExpectingEventVersion)
                tryEnqueueValidWaitingMessage()
                lastActiveTime = Date()
                tryRun()
            } else if (version == this.nextExpectingEventVersion){
                logger.info("{} equals nextExpectingEventVersion ignored, aggregateRootId: {}, aggregateRootTypeName: {}, version: {}, current nextExpectingEventVersion: {}", javaClass.name, aggregateRootId, aggregateRootTypeName, version, nextExpectingEventVersion)
            } else {
                logger.info("{} nextExpectingEventVersion ignored, aggregateRootId: {}, aggregateRootTypeName: {}, version: {}, current nextExpectingEventVersion: {}", javaClass.name, aggregateRootId, aggregateRootTypeName, version, nextExpectingEventVersion)
            }
        }
    }

    private fun enqueueEventStream(processingEvent: ProcessingEvent) {
        synchronized(lockObj) {
            processingEvent.mailbox = this
            processingEventQueue.add(processingEvent)
            nextExpectingEventVersion = processingEvent.message.getVersion() + 1
            if (logger.isDebugEnabled) {
                logger.debug("{} enqueued new message, aggregateRootType: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventTypes: {}, eventIds: {}",
                        javaClass.name,
                        processingEvent.message.getAggregateRootTypeName(),
                        processingEvent.message.getAggregateRootId(),
                        processingEvent.message.commandId,
                        processingEvent.message.getVersion(),
                        processingEvent.message.id,
                        processingEvent.message.events.stream().map { x: IDomainEvent<*> -> x.javaClass.name }.collect(Collectors.joining("|")),
                        processingEvent.message.events.stream().map { x: IDomainEvent<*> -> x.id }.collect(Collectors.joining("|"))
                )
            }
        }
    }

    fun enqueueMessage(processingEvent: ProcessingEvent): EnqueueMessageResult {
        synchronized(lockObj) {
            if (isRemoved()) {
                throw MailBoxProcessException(String.format("ProcessingEventMailBox was removed, cannot allow to enqueue message, aggregateRootTypeName: %s, aggregateRootId: %s", aggregateRootTypeName, aggregateRootId))
            }
            val eventStream = processingEvent.message
            if (nextExpectingEventVersion == null || eventStream.getVersion() > this.nextExpectingEventVersion!!) {
                if (waitingProcessingEventDict.putIfAbsent(eventStream.getVersion(), processingEvent) == null) {
                    logger.warn("{} waiting message added, aggregateRootType: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventTypes: {}, eventIds: {}, nextExpectingEventVersion: {}",
                            javaClass.name,
                            eventStream.getAggregateRootTypeName(),
                            eventStream.getAggregateRootId(),
                            eventStream.commandId,
                            eventStream.getVersion(),
                            eventStream.id,
                            eventStream.events.stream().map { x: IDomainEvent<*> -> x.javaClass.name }.collect(Collectors.joining("|")),
                            eventStream.events.stream().map { obj: IDomainEvent<*> -> obj.id }.collect(Collectors.joining("|")),
                            nextExpectingEventVersion
                    )
                }
                return EnqueueMessageResult.AddToWaitingList
            } else if (eventStream.getVersion() == nextExpectingEventVersion) {
                enqueueEventStream(processingEvent)
                tryEnqueueValidWaitingMessage()
                lastActiveTime = Date()
                tryRun()
                return EnqueueMessageResult.Success
            }
            return EnqueueMessageResult.Ignored
        }
    }

    /**
     * 尝试运行一次MailBox，一次运行会处理一个消息或者一批消息，当前MailBox不能是运行中或者暂停中或者已暂停
     */
    fun tryRun() {
        synchronized(lockObj) {
            if (isRunning()) {
                return
            }
            setAsRunning()
            if (logger.isDebugEnabled) {
                logger.debug("{} start run, aggregateRootId: {}", javaClass.name, aggregateRootId)
            }
            CoroutineScope(Dispatchers.Default).launch { processMessages() }
        }
    }

    /**
     * 请求完成MailBox的单次运行，如果MailBox中还有剩余消息，则继续尝试运行下一次
     */
    fun completeRun() {
        lastActiveTime = Date()
        if (logger.isDebugEnabled) {
            logger.debug("{} complete run, aggregateRootId: {}", javaClass.name, aggregateRootId)
        }
        setAsNotRunning()
        if (getTotalUnHandledMessageCount() > 0) {
            tryRun()
        }
    }

    fun isInactive(timeoutSeconds: Int): Boolean {
        return SystemClock.now() - lastActiveTime.time >= timeoutSeconds
    }

    private fun processMessages() {
        val message = processingEventQueue.poll()
        if (message != null) {
            lastActiveTime = Date()
            try {
                handleProcessingEventAction.apply(message)
            } catch (ex: Exception) {
                logger.error("{} run has unknown exception, aggregateRootId: {}", javaClass.name, aggregateRootId, ex)
                Task.sleep(1)
                completeRun()
            }
        } else {
            completeRun()
        }
    }

    fun tryUsing(): Boolean {
        return isUsing.compareAndSet(0, 1)
    }

    fun exitUsing() {
        isUsing.set(0)
    }

    fun markAsRemoved() {
        isRemoved.set(1)
    }

    private fun setAsRunning() {
        isRunning.set(1)
    }

    fun isRunning(): Boolean {
        return isRunning.get() == 1
    }

    fun isRemoved(): Boolean {
        return isRemoved.get() == 1
    }

    private fun setAsNotRunning() {
        isRunning.set(0)
    }

    fun getWaitingMessageCount(): Int {
        return waitingProcessingEventDict.size
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ProcessingEventMailBox::class.java)
    }

    init {
        this.lastActiveTime = Date()
    }
}
package org.enodeframework.mongo

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.mongodb.MongoBulkWriteException
import com.mongodb.MongoWriteException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.result.InsertManyResult
import com.mongodb.reactivestreams.client.MongoClient
import org.bson.Document
import org.enodeframework.common.EventStoreConfiguration
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.io.IOHelper.tryAsyncActionRecursively
import org.enodeframework.common.io.IOHelper.tryIOFuncAsync
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.eventing.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * @author anruence@gmail.com
 */
class MongoEventStore(
    private val mongoClient: MongoClient,
    private val configuration: EventStoreConfiguration,
    private val eventSerializer: IEventSerializer,
    private val serializeService: ISerializeService
) : IEventStore {
    constructor(
        mongoClient: MongoClient,
        eventSerializer: IEventSerializer,
        serializeService: ISerializeService
    ) : this(mongoClient, EventStoreConfiguration.mongo(), eventSerializer, serializeService)

    private val code = 11000

    override fun batchAppendAsync(eventStreams: List<DomainEventStream>): CompletableFuture<EventAppendResult> {
        val future = CompletableFuture<EventAppendResult>()
        val appendResult = EventAppendResult()
        if (eventStreams.isEmpty()) {
            future.complete(appendResult)
            return future
        }
        val eventStreamMap = eventStreams.stream().distinct()
            .collect(Collectors.groupingBy { obj: DomainEventStream -> obj.aggregateRootId })
        val batchAggregateEventAppendResult = BatchAggregateEventAppendResult(eventStreamMap.keys.size)
        for ((key, value) in eventStreamMap) {
            batchAppendAggregateEventsAsync(key, value, batchAggregateEventAppendResult, 0)
        }
        return batchAggregateEventAppendResult.taskCompletionSource
    }

    private fun batchAppendAggregateEventsAsync(
        aggregateRootId: String,
        eventStreamList: List<DomainEventStream>,
        batchAggregateEventAppendResult: BatchAggregateEventAppendResult,
        retryTimes: Int
    ) {
        tryAsyncActionRecursively(
            "BatchAppendAggregateEventsAsync",
            { batchAppendAggregateEventsAsync(aggregateRootId, eventStreamList) },
            { result: AggregateEventAppendResult? ->
                batchAggregateEventAppendResult.addCompleteAggregate(
                    aggregateRootId,
                    result
                )
            },
            { String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size) },
            null,
            retryTimes, true
        )
    }

    private fun tryFindEventByCommandIdAsync(
        aggregateRootId: String,
        commandId: String,
        duplicateCommandIds: MutableList<String>,
        retryTimes: Int
    ): CompletableFuture<DomainEventStream?> {
        val future = CompletableFuture<DomainEventStream?>()
        tryAsyncActionRecursively(
            "TryFindEventByCommandIdAsync",
            { findAsync(aggregateRootId, commandId) },
            { result: DomainEventStream? ->
                if (result != null) {
                    duplicateCommandIds.add(result.commandId)
                }
                future.complete(result)
            },
            { String.format("[aggregateRootId:%s, commandId:%s]", aggregateRootId, commandId) },
            null,
            retryTimes, true
        )
        return future
    }

    private fun batchAppendAggregateEventsAsync(
        aggregateRootId: String,
        eventStreamList: List<DomainEventStream>
    ): CompletableFuture<AggregateEventAppendResult> {
        val documents: MutableList<Document> = Lists.newArrayList()
        for (domainEventStream in eventStreamList) {
            val document = Document()
            document["aggregateRootId"] = domainEventStream.aggregateRootId
            document["aggregateRootTypeName"] = domainEventStream.aggregateRootTypeName
            document["commandId"] = domainEventStream.commandId
            document["version"] = domainEventStream.version
            document["gmtCreate"] = domainEventStream.timestamp
            document["events"] = serializeService.serialize(eventSerializer.serialize(domainEventStream.events()))
            documents.add(document)
        }

        val future = CompletableFuture<AggregateEventAppendResult>()
        mongoClient.getDatabase(configuration.dbName)
            .getCollection(configuration.eventTableName)
            .insertMany(documents).subscribe(object : Subscriber<InsertManyResult?> {

                override fun onSubscribe(s: Subscription) {
                    s.request(1)
                }

                override fun onNext(insertManyResult: InsertManyResult?) {

                }

                override fun onError(throwable: Throwable) {
                    var sqlCode = 0
                    var message = ""
                    if (throwable is MongoWriteException) {
                        sqlCode = throwable.code
                        message = throwable.message!!
                    }
                    if (throwable is MongoBulkWriteException) {
                        if (throwable.writeErrors.size >= 1) {
                            val writeError = throwable.writeErrors[0]
                            sqlCode = writeError.code
                            message = writeError.message
                        }
                    }
                    if (sqlCode == code && message.contains(configuration.eventVersionUkName)) {
                        val appendResult = AggregateEventAppendResult()
                        appendResult.eventAppendStatus = EventAppendStatus.DuplicateEvent
                        future.complete(appendResult)
                        return
                    }
                    if (sqlCode == code && message.contains(configuration.eventCommandIdUkName)) {
                        val appendResult = AggregateEventAppendResult()
                        appendResult.eventAppendStatus = EventAppendStatus.DuplicateCommand
                        val commandId = getDuplicatedId(message)
                        if (!Strings.isNullOrEmpty(commandId)) {
                            appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
                            future.complete(appendResult)
                            return
                        }
                    }
                    logger.error("Batch append event has unknown exception.", throwable)
                    future.completeExceptionally(EventStoreException(throwable))
                    return
                }

                override fun onComplete() {
                    val appendResult = AggregateEventAppendResult()
                    appendResult.eventAppendStatus = EventAppendStatus.Success
                    future.complete(appendResult)
                }


            })
        return future
    }

    override fun queryAggregateEventsAsync(
        aggregateRootId: String,
        aggregateRootTypeName: String,
        minVersion: Int,
        maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        return tryIOFuncAsync({
            val future = CompletableFuture<List<DomainEventStream>>()
            val filter = Filters.and(
                Filters.eq("aggregateRootId", aggregateRootId),
                Filters.gte("version", minVersion),
                Filters.lte("version", maxVersion)
            )
            val sort = Sorts.ascending("version")
            mongoClient.getDatabase(configuration.dbName).getCollection(configuration.eventTableName)
                .find(filter).sort(sort).subscribe(object : Subscriber<Document> {
                    val streams: MutableList<DomainEventStream> = Lists.newArrayList()
                    override fun onSubscribe(s: Subscription) {
                        s.request(Long.MAX_VALUE)
                    }

                    override fun onNext(document: Document) {
                        val eventStream = DomainEventStream(
                            document.getString("commandId"),
                            document.getString("aggregateRootId"),
                            document.getString("aggregateRootTypeName"),
                            document.get("gmtCreate", Date::class.java),
                            eventSerializer.deserialize(
                                (serializeService.deserialize(
                                    document.getString("events"),
                                    MutableMap::class.java
                                ) as MutableMap<String, String>)
                            ),
                            Maps.newHashMap()
                        )
                        streams.add(eventStream)
                    }

                    override fun onError(throwable: Throwable) {
                        if (throwable is MongoWriteException) {
                            val errorMessage = String.format(
                                "Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s",
                                aggregateRootId,
                                aggregateRootTypeName
                            )
                            logger.error(errorMessage, throwable)
                            future.completeExceptionally(IORuntimeException(throwable))
                            return
                        }
                        logger.error(
                            "Failed to query aggregate events async, aggregateRootId: {}, aggregateRootType: {}",
                            aggregateRootId,
                            aggregateRootTypeName,
                            throwable
                        )
                        future.completeExceptionally(EventStoreException(throwable))
                        return
                    }

                    override fun onComplete() {
                        streams.sortWith(Comparator.comparingInt { obj: DomainEventStream -> obj.version })
                        future.complete(streams)
                    }
                })
            future
        }, "QueryAggregateEventsAsync")
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return tryIOFuncAsync({
            val future = CompletableFuture<DomainEventStream?>()
            val filter = Filters.and(Filters.eq("aggregateRootId", aggregateRootId), Filters.eq("version", version))
            mongoClient.getDatabase(configuration.dbName).getCollection(configuration.eventTableName)
                .find(filter).subscribe(object : Subscriber<Document> {

                    private var eventStream: DomainEventStream? = null

                    override fun onSubscribe(s: Subscription) {
                        s.request(1)
                    }

                    override fun onNext(document: Document) {
                        val eventStream = DomainEventStream(
                            document.getString("commandId"),
                            document.getString("aggregateRootId"),
                            document.getString("aggregateRootTypeName"),
                            document.get("gmtCreate", Date::class.java),
                            eventSerializer.deserialize(
                                serializeService.deserialize(
                                    document.getString("events"),
                                    MutableMap::class.java
                                ) as MutableMap<String, String>
                            ),
                            Maps.newHashMap()
                        )
                        this.eventStream = eventStream
                        future.complete(eventStream)
                        return
                    }

                    override fun onError(throwable: Throwable) {
                        if (throwable is MongoWriteException) {
                            logger.error(
                                "Find event by version has sql exception, aggregateRootId: {}, version: {}",
                                aggregateRootId,
                                version,
                                throwable
                            )
                            future.completeExceptionally(IORuntimeException(throwable))
                            return
                        }
                        logger.error(
                            "Find event by version has unknown exception, aggregateRootId: {}, version: {}",
                            aggregateRootId,
                            version,
                            throwable
                        )
                        future.completeExceptionally(EventStoreException(throwable))
                        return
                    }

                    override fun onComplete() {
                        future.complete(eventStream)
                        return
                    }
                })
            future
        }, "FindEventByVersionAsync")
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return tryIOFuncAsync({
            val future = CompletableFuture<DomainEventStream?>()
            val filter = Filters.and(Filters.eq("aggregateRootId", aggregateRootId), Filters.eq("commandId", commandId))
            mongoClient.getDatabase(configuration.dbName).getCollection(configuration.eventTableName)
                .find(filter).subscribe(object : Subscriber<Document> {

                    private var eventStream: DomainEventStream? = null

                    override fun onSubscribe(s: Subscription) {
                        s.request(1)
                    }

                    override fun onNext(document: Document) {
                        val eventStream = DomainEventStream(
                            document.getString("commandId"),
                            document.getString("aggregateRootId"),
                            document.getString("aggregateRootTypeName"),
                            document.get("gmtCreate", Date::class.java),
                            eventSerializer.deserialize(
                                serializeService.deserialize(
                                    document.getString("events"),
                                    MutableMap::class.java
                                ) as MutableMap<String, String>
                            ),
                            Maps.newHashMap()
                        )
                        this.eventStream = eventStream
                        future.complete(eventStream)
                        return
                    }

                    override fun onError(throwable: Throwable) {

                        if (throwable is MongoWriteException) {
                            logger.error(
                                "Find event by commandId has sql exception, aggregateRootId: {}, commandId: {}",
                                aggregateRootId,
                                commandId,
                                throwable
                            )
                            future.completeExceptionally(IORuntimeException(throwable))
                            return
                        }
                        logger.error(
                            "Find event by commandId has unknown exception, aggregateRootId: {}, commandId: {}",
                            aggregateRootId,
                            commandId,
                            throwable
                        )
                        future.completeExceptionally(EventStoreException(throwable))
                        return
                    }

                    override fun onComplete() {
                        future.complete(eventStream)
                        return
                    }
                })
            future
        }, "FindEventByCommandIdAsync")
    }

    /**
     * E11000 duplicate key error collection: enode.event_stream index: aggregateRootId_1_commandId_1 dup key: { aggregateRootId: "5ee8b610d7671114741829c7", commandId: "5ee8b61bd7671114741829cf" }
     */
    private fun getDuplicatedId(message: String): String {
        val matcher = PATTERN_MONGO.matcher(message)
        if (matcher.find()) {
            if (matcher.groupCount() == 1) {
                return matcher.group(1)
            }
        }
        return ""
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MongoEventStore::class.java)
        private val PATTERN_MONGO = Pattern.compile("\\{.+?commandId: \"(.+?)\" }$")
    }
}
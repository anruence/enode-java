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
class MongoEventStore(private val mongoClient: MongoClient, private val mongoConfiguration: MongoConfiguration, private val eventSerializer: IEventSerializer, private val serializeService: ISerializeService) : IEventStore, SQLDialect {
    private val duplicateCode: Int = mongoConfiguration.duplicateCode
    private val versionIndexName: String = mongoConfiguration.eventTableVersionUniqueIndexName
    private val commandIndexName: String = mongoConfiguration.eventTableCommandIdUniqueIndexName

    constructor(mongoClient: MongoClient, eventSerializer: IEventSerializer, serializeService: ISerializeService) : this(mongoClient, MongoConfiguration(), eventSerializer, serializeService)

    override fun batchAppendAsync(eventStreams: List<DomainEventStream>): CompletableFuture<EventAppendResult> {
        val future = CompletableFuture<EventAppendResult>()
        val appendResult = EventAppendResult()
        if (eventStreams.isEmpty()) {
            future.complete(appendResult)
            return future
        }
        val eventStreamMap = eventStreams.stream().distinct().collect(Collectors.groupingBy { obj: DomainEventStream -> obj.aggregateRootId })
        val batchAggregateEventAppendResult = BatchAggregateEventAppendResult(eventStreamMap.keys.size)
        for ((key, value) in eventStreamMap) {
            batchAppendAggregateEventsAsync(key, value, batchAggregateEventAppendResult, 0)
        }
        return batchAggregateEventAppendResult.taskCompletionSource
    }

    private fun batchAppendAggregateEventsAsync(aggregateRootId: String, eventStreamList: List<DomainEventStream>, batchAggregateEventAppendResult: BatchAggregateEventAppendResult, retryTimes: Int) {
        tryAsyncActionRecursively("BatchAppendAggregateEventsAsync",
                { batchAppendAggregateEventsAsync(aggregateRootId, eventStreamList) },
                { result: AggregateEventAppendResult? -> batchAggregateEventAppendResult.addCompleteAggregate(aggregateRootId, result) },
                { String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size) },
                null,
                retryTimes, true)
    }

    private fun tryFindEventByCommandIdAsync(aggregateRootId: String, commandId: String, duplicateCommandIds: MutableList<String>, retryTimes: Int): CompletableFuture<DomainEventStream?> {
        val future = CompletableFuture<DomainEventStream?>()
        tryAsyncActionRecursively("TryFindEventByCommandIdAsync",
                { findAsync(aggregateRootId, commandId) },
                { result: DomainEventStream? ->
                    if (result != null) {
                        duplicateCommandIds.add(result.commandId)
                    }
                    future.complete(result)
                },
                { String.format("[aggregateRootId:%s, commandId:%s]", aggregateRootId, commandId) },
                null,
                retryTimes, true)
        return future
    }

    private fun batchAppendAggregateEventsAsync(aggregateRootId: String, eventStreamList: List<DomainEventStream>): CompletableFuture<AggregateEventAppendResult> {
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
        val future = batchInsertAsync(documents)
        return future.exceptionally { throwable: Throwable ->
            var code = 0
            var message = ""
            if (throwable is MongoWriteException) {
                code = throwable.code
                message = throwable.message!!
            }
            if (throwable is MongoBulkWriteException) {
                if (throwable.writeErrors.size >= 1) {
                    val writeError = throwable.writeErrors[0]
                    code = writeError.code
                    message = writeError.message
                }
            }
            if (code == duplicateCode && message.contains(versionIndexName)) {
                val appendResult = AggregateEventAppendResult()
                appendResult.eventAppendStatus = EventAppendStatus.DuplicateEvent
                return@exceptionally appendResult
            }
            if (code == duplicateCode && message.contains(commandIndexName)) {
                val appendResult = AggregateEventAppendResult()
                appendResult.eventAppendStatus = EventAppendStatus.DuplicateCommand
                val commandId = getDuplicatedId(throwable)
                if (!Strings.isNullOrEmpty(commandId)) {
                    appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
                    return@exceptionally appendResult
                }
                return@exceptionally appendResult
            }
            logger.error("Batch append event has unknown exception.", throwable)
            throw EventStoreException(throwable)
        }
    }

    private fun batchInsertAsync(documents: List<Document>?): CompletableFuture<AggregateEventAppendResult> {
        val future = CompletableFuture<AggregateEventAppendResult>()
        mongoClient.getDatabase(mongoConfiguration.databaseName)
                .getCollection(mongoConfiguration.eventCollectionName)
                .insertMany(documents).subscribe(object : Subscriber<InsertManyResult?> {
                    override fun onSubscribe(s: Subscription) {
                        s.request(1)
                    }

                    override fun onNext(insertManyResult: InsertManyResult?) {
                        val appendResult = AggregateEventAppendResult()
                        appendResult.eventAppendStatus = EventAppendStatus.Success
                        future.complete(appendResult)
                    }

                    override fun onError(t: Throwable) {
                        future.completeExceptionally(t)
                    }

                    override fun onComplete() {
                        val appendResult = AggregateEventAppendResult()
                        appendResult.eventAppendStatus = EventAppendStatus.Success
                        future.complete(appendResult)
                    }
                })
        return future
    }

    override fun queryAggregateEventsAsync(aggregateRootId: String, aggregateRootTypeName: String, minVersion: Int, maxVersion: Int): CompletableFuture<List<DomainEventStream>> {
        return tryIOFuncAsync({
            val future = CompletableFuture<List<DomainEventStream>>()
            val filter = Filters.and(Filters.eq("aggregateRootId", aggregateRootId),
                    Filters.gte("version", minVersion),
                    Filters.lte("version", maxVersion))
            val sort = Sorts.ascending("version")
            mongoClient.getDatabase(mongoConfiguration.databaseName).getCollection(mongoConfiguration.eventCollectionName)
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
                                    eventSerializer.deserialize(serializeService.deserialize(document.getString("events"), MutableMap::class.java) as MutableMap<String, String>?),
                                    Maps.newHashMap())
                            streams.add(eventStream)
                        }

                        override fun onError(t: Throwable) {
                            future.completeExceptionally(t)
                        }

                        override fun onComplete() {
                            streams.sortWith(Comparator.comparingInt { obj: DomainEventStream -> obj.version })
                            future.complete(streams)
                        }
                    })
            future.exceptionally { throwable: Throwable? ->
                if (throwable is MongoWriteException) {
                    val errorMessage = String.format("Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s", aggregateRootId, aggregateRootTypeName)
                    logger.error(errorMessage, throwable)
                    throw IORuntimeException(throwable)
                }
                logger.error("Failed to query aggregate events async, aggregateRootId: {}, aggregateRootType: {}", aggregateRootId, aggregateRootTypeName, throwable)
                throw EventStoreException(throwable)
            }
        }, "QueryAggregateEventsAsync")
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return tryIOFuncAsync({
            val future = CompletableFuture<DomainEventStream?>()
            val filter = Filters.and(Filters.eq("aggregateRootId", aggregateRootId), Filters.eq("version", version))
            mongoClient.getDatabase(mongoConfiguration.databaseName).getCollection(mongoConfiguration.eventCollectionName)
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
                                    eventSerializer.deserialize(serializeService.deserialize(document.getString("events"), MutableMap::class.java) as MutableMap<String, String>?),
                                    Maps.newHashMap())
                            this.eventStream = eventStream
                            future.complete(eventStream)
                        }

                        override fun onError(t: Throwable) {
                            future.completeExceptionally(t)
                        }

                        override fun onComplete() {
                            future.complete(eventStream)
                        }
                    })
            future.exceptionally { throwable: Throwable? ->
                if (throwable is MongoWriteException) {
                    logger.error("Find event by version has sql exception, aggregateRootId: {}, version: {}", aggregateRootId, version, throwable)
                    throw IORuntimeException(throwable)
                }
                logger.error("Find event by version has unknown exception, aggregateRootId: {}, version: {}", aggregateRootId, version, throwable)
                throw EventStoreException(throwable)
            }
        }, "FindEventByVersionAsync")
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return tryIOFuncAsync({
            val future = CompletableFuture<DomainEventStream?>()
            val filter = Filters.and(Filters.eq("aggregateRootId", aggregateRootId), Filters.eq("commandId", commandId))
            mongoClient.getDatabase(mongoConfiguration.databaseName).getCollection(mongoConfiguration.eventCollectionName)
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
                                    eventSerializer.deserialize(serializeService.deserialize(document.getString("events"), MutableMap::class.java) as MutableMap<String, String>?),
                                    Maps.newHashMap())
                            this.eventStream = eventStream
                            future.complete(eventStream)
                        }

                        override fun onError(t: Throwable) {
                            future.completeExceptionally(t)
                        }

                        override fun onComplete() {
                            future.complete(eventStream)
                        }
                    })
            future.exceptionally { throwable: Throwable? ->
                if (throwable is MongoWriteException) {
                    logger.error("Find event by commandId has sql exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, throwable)
                    throw IORuntimeException(throwable)
                }
                logger.error("Find event by commandId has unknown exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, throwable)
                throw EventStoreException(throwable)
            }
        }, "FindEventByCommandIdAsync")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MongoEventStore::class.java)
        private val PATTERN_MONGO = Pattern.compile("\\{.+?commandId: \"(.+?)\" }$")
    }

    /**
     * E11000 duplicate key error collection: enode.event_stream index: aggregateRootId_1_commandId_1 dup key: { aggregateRootId: "5ee8b610d7671114741829c7", commandId: "5ee8b61bd7671114741829cf" }
     */
    override fun getDuplicatedId(throwable: Throwable): String {
        val matcher = PATTERN_MONGO.matcher(throwable.message!!)
        if (matcher.find()) {
            if (matcher.groupCount() == 1) {
                return matcher.group(1)
            }
        }
        return ""
    }

}
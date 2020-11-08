package org.enodeframework.jdbc

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.common.utilities.Ensure
import org.enodeframework.eventing.*
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
abstract class JDBCEventStore(dataSource: DataSource, dbConfiguration: DBConfiguration, eventSerializer: IEventSerializer, private val serializeService: ISerializeService) : CoroutineVerticle(), IEventStore {
    private val tableName: String
    private val sqlState: String
    private val versionIndexName: String
    private val commandIndexName: String

    /**
     * 可以使用分库分表的ShardDataSource，不应该在此处提供能力
     */
    private val dataSource: DataSource
    private val eventSerializer: IEventSerializer
    private lateinit var sqlClient: SQLClient

    constructor(dataSource: DataSource, eventSerializer: IEventSerializer, serializeService: ISerializeService) : this(dataSource, DBConfiguration(), eventSerializer, serializeService)

    override suspend fun start() {
        sqlClient = JDBCClient.create(vertx, dataSource)
    }

    override suspend fun stop() {
        sqlClient.close()
    }

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

    fun batchAppendAggregateEventsAsync(aggregateRootId: String, eventStreamList: List<DomainEventStream>, batchAggregateEventAppendResult: BatchAggregateEventAppendResult, retryTimes: Int) {
        IOHelper.tryAsyncActionRecursively("BatchAppendAggregateEventsAsync",
                { batchAppendAggregateEventsAsync(aggregateRootId, eventStreamList) },
                { result: AggregateEventAppendResult? -> batchAggregateEventAppendResult.addCompleteAggregate(aggregateRootId, result) },
                { String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size) },
                null,
                retryTimes, true)
    }

    private fun tryFindEventByCommandIdAsync(aggregateRootId: String, commandId: String, duplicateCommandIds: MutableList<String>, retryTimes: Int): CompletableFuture<DomainEventStream?> {
        val future = CompletableFuture<DomainEventStream?>()
        IOHelper.tryAsyncActionRecursively("TryFindEventByCommandIdAsync",
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

    fun batchAppendAggregateEventsAsync(aggregateRootId: String, eventStreamList: List<DomainEventStream>): CompletableFuture<AggregateEventAppendResult?> {
        val sql = String.format(INSERT_EVENT_SQL, tableName)
        val jsonArrays: ArrayList<JsonArray> = Lists.newArrayList()
        for (domainEventStream in eventStreamList) {
            val array = JsonArray()
            array.add(domainEventStream.aggregateRootId)
            array.add(domainEventStream.aggregateRootTypeName)
            array.add(domainEventStream.commandId)
            array.add(domainEventStream.version)
            array.add(domainEventStream.timestamp.toInstant())
            array.add(serializeService.serialize(eventSerializer.serialize(domainEventStream.events())))
            jsonArrays.add(array)
        }
        val future = batchInsertAsync(sql, jsonArrays)
        return future.exceptionally { throwable: Throwable? ->
            if (throwable is SQLException) {
                if (sqlState == throwable.sqlState && Objects.nonNull(throwable.message) && throwable.message!!.contains(versionIndexName)) {
                    val appendResult = AggregateEventAppendResult()
                    appendResult.eventAppendStatus = EventAppendStatus.DuplicateEvent
                    return@exceptionally appendResult
                }
                if (sqlState == throwable.sqlState && throwable.message!!.contains(commandIndexName)) {
                    // Duplicate entry '5d3ac841d1fcfe669e9a257d-5d3ac841d1fcfe669e9a2585' for key 'IX_EventStream_AggId_CommandId'
                    // ERROR: duplicate key value violates unique constraint "event_stream_aggregate_root_id_command_id_key"\n详细：Key (aggregate_root_id, command_id)=(5ee99656d767113d73a7540f, 5ee99656d767113d73a75417) already exists.
                    val appendResult = AggregateEventAppendResult()
                    appendResult.eventAppendStatus = EventAppendStatus.DuplicateCommand
                    val commandId = parseDuplicateCommandId(throwable.message!!)
                    if (!Strings.isNullOrEmpty(commandId)) {
                        appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
                        return@exceptionally appendResult
                    }
                    // 如果没有从异常信息获取到commandId(很低概率获取不到)，兜底从db查询出来，此时选择的策略是从最后一个开始查
                    // 但是Vert.x的线程模型决定了不能再次使用eventloop线程执行阻塞的查询操作
                    tryFindEventByCommandIdAsyncRecursion(eventStreamList.size, aggregateRootId, eventStreamList, appendResult.duplicateCommandIds, 0)
                    return@exceptionally appendResult
                }
                logger.error("Batch append event has sql exception.", throwable)
                throw IORuntimeException(throwable)
            }
            logger.error("Batch append event has unknown exception.", throwable)
            throw EventStoreException(throwable)
        }
    }

    private fun tryFindEventByCommandIdAsyncRecursion(i: Int, aggregateRootId: String, eventStreamList: List<DomainEventStream>, duplicateCommandIds: MutableList<String>, retryTimes: Int) {
        if (i == 0) {
            return
        }
        val eventStream = eventStreamList[i - 1]
        tryFindEventByCommandIdAsync(aggregateRootId, eventStream.commandId, duplicateCommandIds, retryTimes).thenAccept { x: DomainEventStream? ->
            if (x != null) {
                return@thenAccept
            }
            tryFindEventByCommandIdAsyncRecursion(i - 1, aggregateRootId, eventStreamList, duplicateCommandIds, 0)
        }
    }

    protected abstract fun parseDuplicateCommandId(msg: String): String

    fun batchInsertAsync(sql: String, jsonArrays: List<JsonArray>): CompletableFuture<AggregateEventAppendResult?> {
        val future = CompletableFuture<AggregateEventAppendResult?>()
        batchWithParams(sql, jsonArrays) { x: AsyncResult<List<Int?>?> ->
            if (x.succeeded()) {
                val appendResult = AggregateEventAppendResult()
                appendResult.eventAppendStatus = EventAppendStatus.Success
                future.complete(appendResult)
                return@batchWithParams
            }
            future.completeExceptionally(x.cause())
        }
        return future;
    }

    override fun queryAggregateEventsAsync(aggregateRootId: String, aggregateRootTypeName: String, minVersion: Int, maxVersion: Int): CompletableFuture<List<DomainEventStream>> {
        return IOHelper.tryIOFuncAsync({
            val future = CompletableFuture<List<DomainEventStream>>()
            val sql = String.format(SELECT_MANY_BY_VERSION_SQL, tableName)
            val array = JsonArray()
            array.add(aggregateRootId)
            array.add(minVersion)
            array.add(maxVersion)
            sqlClient.queryWithParams(sql, array) { x: AsyncResult<ResultSet> ->
                if (x.succeeded()) {
                    val results: MutableList<StreamRecord> = Lists.newArrayList()
                    x.result().rows.forEach(Consumer { row: JsonObject -> results.add(row.mapTo(StreamRecord::class.java)) })
                    val streams = results.stream().map { record: StreamRecord -> convertFrom(record) }.collect(Collectors.toList())
                    future.complete(streams)
                    return@queryWithParams
                }
                future.completeExceptionally(x.cause())
            }
            future.exceptionally { throwable: Throwable? ->
                if (throwable is SQLException) {
                    val errorMessage = String.format("Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s", aggregateRootId, aggregateRootTypeName)
                    logger.error(errorMessage, throwable)
                    throw IORuntimeException(throwable)
                }
                logger.error("Failed to query aggregate events async, aggregateRootId: {}, aggregateRootType: {}", aggregateRootId, aggregateRootTypeName, throwable)
                throw EventStoreException(throwable)
            }
        }, "QueryAggregateEventsAsync")
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream> {
        return IOHelper.tryIOFuncAsync({
            val future = CompletableFuture<DomainEventStream>()
            val sql = String.format(SELECT_ONE_BY_VERSION_SQL, tableName)
            val array = JsonArray()
            array.add(aggregateRootId)
            array.add(version)
            sqlClient.queryWithParams(sql, array) { x: AsyncResult<ResultSet> ->
                if (x.succeeded()) {
                    var stream: DomainEventStream? = null
                    val first = x.result().rows.stream().findFirst()
                    if (first.isPresent) {
                        val record = first.get().mapTo(StreamRecord::class.java)
                        stream = convertFrom(record)
                    }
                    future.complete(stream)
                    return@queryWithParams
                }
                future.completeExceptionally(x.cause())
            }
            future.exceptionally { throwable: Throwable? ->
                if (throwable is SQLException) {
                    logger.error("Find event by version has sql exception, aggregateRootId: {}, version: {}", aggregateRootId, version, throwable)
                    throw IORuntimeException(throwable)
                }
                logger.error("Find event by version has unknown exception, aggregateRootId: {}, version: {}", aggregateRootId, version, throwable)
                throw EventStoreException(throwable)
            }
        }, "FindEventByVersionAsync")
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream> {
        return IOHelper.tryIOFuncAsync({
            val future = CompletableFuture<DomainEventStream>()
            val sql = String.format(SELECT_ONE_BY_COMMAND_ID_SQL, tableName)
            val array = JsonArray()
            array.add(aggregateRootId)
            array.add(commandId)
            sqlClient.queryWithParams(sql, array) { x: AsyncResult<ResultSet> ->
                if (x.succeeded()) {
                    var stream: DomainEventStream? = null
                    val first = x.result().rows.stream().findFirst()
                    if (first.isPresent) {
                        val record = first.get().mapTo(StreamRecord::class.java)
                        stream = convertFrom(record)
                    }
                    future.complete(stream)
                    return@queryWithParams
                }
                future.completeExceptionally(x.cause())
            }
            future.exceptionally { throwable: Throwable? ->
                if (throwable is SQLException) {
                    logger.error("Find event by commandId has sql exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, throwable)
                    throw IORuntimeException(throwable)
                }
                logger.error("Find event by commandId has unknown exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, throwable)
                throw EventStoreException(throwable)
            }
        }, "FindEventByCommandIdAsync")
    }

    override fun getPublishedVersionAsync(aggregateRootTypeName: String, aggregateRootId: String): CompletableFuture<Int> {
        return IOHelper.tryIOFuncAsync({
            val future = CompletableFuture<Int>()
            val sql = String.format(SELECT_MAX_VERSION_SQL, tableName)
            val array = JsonArray()
            array.add(aggregateRootId)
            array.add(aggregateRootTypeName)
            sqlClient.querySingleWithParams(sql, array) { x: AsyncResult<JsonArray?> ->
                if (x.succeeded()) {
                    var result = 0
                    if (x.result() != null && x.result()!!.size() > 0) {
                        result = Optional.ofNullable(x.result()!!.getInteger(0)).orElse(0)
                    }
                    future.complete(result)
                    return@querySingleWithParams
                }
                future.completeExceptionally(x.cause())
            }
            future.exceptionally { throwable: Throwable? ->
                if (throwable is SQLException) {
                    logger.error("Find version by aggregateRootId has sql exception, aggregateRootId: {}, aggregateRootTypeName: {}", aggregateRootId, aggregateRootTypeName, throwable)
                    throw IORuntimeException(throwable)
                }
                logger.error("Find version by aggregateRootId has unknown exception, aggregateRootId: {}, aggregateRootTypeName: {}", aggregateRootId, aggregateRootTypeName, throwable)
                throw EventStoreException(throwable)
            }
        }, "getPublishedVersionAsync")
    }

    private fun convertFrom(record: StreamRecord): DomainEventStream {
        return DomainEventStream(
                record.commandId,
                record.aggregateRootId,
                record.aggregateRootTypeName,
                record.gmtCreated,
                eventSerializer.deserialize(serializeService.deserialize(record.events, MutableMap::class.java) as MutableMap<String, String>?),
                Maps.newHashMap())
    }

    private fun batchWithParams(sql: String, params: List<JsonArray>, handler: Handler<AsyncResult<List<Int?>?>>): SQLClient? {
        sqlClient.getConnection { getConnection: AsyncResult<SQLConnection> ->
            if (getConnection.failed()) {
                handler.handle(Future.failedFuture(getConnection.cause()))
                return@getConnection
            }
            val conn = getConnection.result()
            conn.setAutoCommit(false) { autocommit: AsyncResult<Void?> ->
                if (autocommit.failed()) {
                    handler.handle(Future.failedFuture(autocommit.cause()))
                    resetAutoCommitAndCloseConnection(conn)
                    return@setAutoCommit
                }
                conn.batchWithParams(sql, params) { batch: AsyncResult<List<Int?>?> ->
                    if (batch.succeeded()) {
                        conn.commit { commit: AsyncResult<Void?> ->
                            if (commit.succeeded()) {
                                handler.handle(Future.succeededFuture(batch.result()))
                            } else {
                                handler.handle(Future.failedFuture(commit.cause()))
                            }
                            resetAutoCommitAndCloseConnection(conn)
                        }
                    } else {
                        conn.rollback { rollback: AsyncResult<Void?> ->
                            if (rollback.succeeded()) {
                                handler.handle(Future.failedFuture(batch.cause()))
                            } else {
                                handler.handle(Future.failedFuture(rollback.cause()))
                            }
                            resetAutoCommitAndCloseConnection(conn)
                        }
                    }
                }
            }
        }
        return sqlClient
    }

    private fun resetAutoCommitAndCloseConnection(conn: SQLConnection) {
        conn.setAutoCommit(true) { commit: AsyncResult<Void?> ->
            if (commit.failed()) {
                logger.error("jdbc driver set autocommit true failed", commit.cause())
            }
            // close will put the connection into pool
            conn.close()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JDBCEventStore::class.java)
        private const val INSERT_EVENT_SQL = "INSERT INTO %s (aggregate_root_id, aggregate_root_type_name, command_id, version, gmt_create, events) VALUES (?, ?, ?, ?, ?, ?)"
        private const val SELECT_MANY_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version >= ? AND Version <= ? ORDER BY version"
        private const val SELECT_ONE_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version = ?"
        private const val SELECT_ONE_BY_COMMAND_ID_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND command_id = ?"
        private const val SELECT_MAX_VERSION_SQL = "SELECT MAX(version) AS version FROM %s WHERE aggregate_root_id = ? AND aggregate_root_type_name = ?"
    }

    init {
        Ensure.notNull(dataSource, "dataSource")
        Ensure.notNull(eventSerializer, "eventSerializer")
        Ensure.notNull(dbConfiguration, "dbConfiguration")
        this.eventSerializer = eventSerializer
        this.dataSource = dataSource
        tableName = dbConfiguration.eventTableName
        sqlState = dbConfiguration.sqlState
        versionIndexName = dbConfiguration.eventTableVersionUniqueIndexName
        commandIndexName = dbConfiguration.eventTableCommandIdUniqueIndexName
    }
}
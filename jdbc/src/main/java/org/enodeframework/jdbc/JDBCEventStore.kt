package org.enodeframework.jdbc

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import org.enodeframework.common.DbType
import org.enodeframework.common.EventStoreConfiguration
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.eventing.*
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
class JDBCEventStore(
    private val dataSource: DataSource,
    private val configuration: EventStoreConfiguration,
    private val eventSerializer: IEventSerializer,
    private val serializeService: ISerializeService
) : AbstractVerticle(), IEventStore {

    private lateinit var sqlClient: SQLClient

    private var code: String = ""

    override fun start() {
        super.start()
        this.sqlClient = JDBCClient.create(vertx, this.dataSource)
    }

    override fun stop() {
        super.stop()
        this.sqlClient.close()
    }

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
        IOHelper.tryAsyncActionRecursively(
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
        IOHelper.tryAsyncActionRecursively(
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
        val sql = String.format(INSERT_EVENT_SQL, configuration.eventTableName)
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
        val future = CompletableFuture<AggregateEventAppendResult>()
        batchWithParams(sql, jsonArrays) { ar: AsyncResult<List<Int?>?> ->
            if (ar.succeeded()) {
                val appendResult = AggregateEventAppendResult()
                appendResult.eventAppendStatus = EventAppendStatus.Success
                future.complete(appendResult)
                return@batchWithParams
            }
            val throwable = ar.cause()
            if (throwable is SQLException) {
                if (code == throwable.sqlState && throwable.message?.contains(configuration.eventVersionUkName) == true) {
                    val appendResult = AggregateEventAppendResult()
                    appendResult.eventAppendStatus = EventAppendStatus.DuplicateEvent
                    future.complete(appendResult)
                    return@batchWithParams
                }
                if (code == throwable.sqlState && throwable.message?.contains(configuration.eventCommandIdUkName) == true) {
                    // 不同的数据库在冲突时的错误信息不同，可以通过解析错误信息的方式将冲突的commandId找出来，这里要求id不能命中正则的规则（不包含-字符）
                    val appendResult = AggregateEventAppendResult()
                    appendResult.eventAppendStatus = EventAppendStatus.DuplicateCommand
                    val commandId = this.getDuplicatedId(throwable.message ?: "")
                    if (!Strings.isNullOrEmpty(commandId)) {
                        appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
                    }
                    // 如果没有从异常信息获取到commandId(很低概率获取不到)，需要从db查询出来
                    // 但是Vert.x的线程模型决定了不能再次使用EventLoop线程执行阻塞的查询操作
                    future.complete(appendResult)
                    return@batchWithParams
                }
                logger.error("Batch append event has sql exception.", throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@batchWithParams
            }
            logger.error("Batch append event has unknown exception.", throwable)
            future.completeExceptionally(EventStoreException(throwable))
            return@batchWithParams
        }
        return future
    }

    private fun tryFindEventByCommandIdAsyncRecursion(
        i: Int,
        aggregateRootId: String,
        eventStreamList: List<DomainEventStream>,
        duplicateCommandIds: MutableList<String>,
        retryTimes: Int
    ) {
        if (i == 0) {
            return
        }
        val eventStream = eventStreamList[i - 1]
        tryFindEventByCommandIdAsync(
            aggregateRootId,
            eventStream.commandId,
            duplicateCommandIds,
            retryTimes
        ).thenAccept { x: DomainEventStream? ->
            if (x != null) {
                return@thenAccept
            }
            tryFindEventByCommandIdAsyncRecursion(i - 1, aggregateRootId, eventStreamList, duplicateCommandIds, 0)
        }
    }

    override fun queryAggregateEventsAsync(
        aggregateRootId: String,
        aggregateRootTypeName: String,
        minVersion: Int,
        maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        return IOHelper.tryIOFuncAsync({
            queryAggregateEvents(aggregateRootId, aggregateRootTypeName, minVersion, maxVersion)
        }, "QueryAggregateEventsAsync")
    }

    private fun queryAggregateEvents(
        aggregateRootId: String,
        aggregateRootTypeName: String,
        minVersion: Int,
        maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        val future = CompletableFuture<List<DomainEventStream>>()
        val sql = String.format(SELECT_MANY_BY_VERSION_SQL, configuration.eventTableName)
        val array = JsonArray()
        array.add(aggregateRootId)
        array.add(minVersion)
        array.add(maxVersion)
        sqlClient.queryWithParams(sql, array) { ar: AsyncResult<ResultSet> ->
            if (ar.succeeded()) {
                future.complete(ar.result().rows.map { row ->
                    this.convertFrom(row)
                }.toMutableList())
                return@queryWithParams
            }
            val throwable = ar.cause()
            if (throwable is SQLException) {
                val errorMessage = String.format(
                    "Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s",
                    aggregateRootId,
                    aggregateRootTypeName
                )
                logger.error(errorMessage, throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@queryWithParams
            }
            logger.error(
                "Failed to query aggregate events async, aggregateRootId: {}, aggregateRootType: {}",
                aggregateRootId,
                aggregateRootTypeName,
                throwable
            )
            future.completeExceptionally(EventStoreException(throwable))
            return@queryWithParams
        }
        return future;
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            findByVersion(aggregateRootId, version)
        }, "FindEventByVersionAsync")
    }

    private fun findByVersion(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        val future = CompletableFuture<DomainEventStream?>()
        val sql = String.format(SELECT_ONE_BY_VERSION_SQL, configuration.eventTableName)
        val array = JsonArray()
        array.add(aggregateRootId)
        array.add(version)
        sqlClient.queryWithParams(sql, array) { ar: AsyncResult<ResultSet> ->
            if (ar.succeeded()) {
                ar.result().rows.firstOrNull().let { row ->
                    future.complete(row?.let { this.convertFrom(it) })
                    return@queryWithParams
                }
            }
            val throwable = ar.cause()
            if (throwable is SQLException) {
                logger.error(
                    "Find event by version has sql exception, aggregateRootId: {}, version: {}",
                    aggregateRootId,
                    version,
                    throwable
                )
                future.completeExceptionally(IORuntimeException(throwable))
                return@queryWithParams
            }
            logger.error(
                "Find event by version has unknown exception, aggregateRootId: {}, version: {}",
                aggregateRootId,
                version,
                throwable
            )
            future.completeExceptionally(EventStoreException(throwable))
            return@queryWithParams
        }
        return future
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            findByCommandId(aggregateRootId, commandId)
        }, "FindEventByCommandIdAsync")
    }

    private fun findByCommandId(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        val future = CompletableFuture<DomainEventStream?>()
        val sql = String.format(SELECT_ONE_BY_COMMAND_ID_SQL, configuration.eventTableName)
        val array = JsonArray()
        array.add(aggregateRootId)
        array.add(commandId)
        sqlClient.queryWithParams(sql, array) { ar: AsyncResult<ResultSet> ->
            if (ar.succeeded()) {
                ar.result().rows.firstOrNull().let { row ->
                    future.complete(row?.let { this.convertFrom(it) })
                    return@queryWithParams
                }
            }
            val throwable = ar.cause()
            if (throwable is SQLException) {
                logger.error(
                    "Find event by commandId has sql exception, aggregateRootId: {}, commandId: {}",
                    aggregateRootId,
                    commandId,
                    throwable
                )
                future.completeExceptionally(IORuntimeException(throwable))
                return@queryWithParams
            }
            logger.error(
                "Find event by commandId has unknown exception, aggregateRootId: {}, commandId: {}",
                aggregateRootId,
                commandId,
                throwable
            )
            future.completeExceptionally(EventStoreException(throwable))
            return@queryWithParams
        }
        return future

    }

    private fun convertFrom(record: JsonObject): DomainEventStream {
        return DomainEventStream(
            record.getString("command_id"),
            record.getString("aggregate_root_id"),
            record.getString("aggregate_root_type_name"),
            Date.from(ZonedDateTime.parse(record.getString("gmt_create")).toInstant()),
            eventSerializer.deserialize(
                serializeService.deserialize(
                    record.getString("events"),
                    MutableMap::class.java
                ) as MutableMap<String, String>
            ),
            Maps.newHashMap()
        )
    }

    private fun batchWithParams(
        sql: String,
        params: List<JsonArray>,
        handler: Handler<AsyncResult<List<Int?>?>>
    ): SQLClient {
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

    // Duplicate entry '5d3ac841d1fcfe669e9a257d-5d3ac841d1fcfe669e9a2585' for key 'IX_EventStream_AggId_CommandId'
    // ERROR: duplicate key value violates unique constraint "event_stream_aggregate_root_id_command_id_key"\n详细：Key (aggregate_root_id, command_id)=(5ee99656d767113d73a7540f, 5ee99656d767113d73a75417) already exists.
    private fun getDuplicatedId(message: String): String {
        if (DbType.MySQL.name == configuration.dbType) {
            return getDuplicatedIdFromMySQL(message)
        }
        if (DbType.Pg.name == configuration.dbType) {
            return getDuplicatedIdFromPg(message)
        }
        return ""
    }

    private fun getDuplicatedIdFromMySQL(message: String): String {
        val matcher = PATTERN_MYSQL.matcher(message)
        if (!matcher.find()) {
            return ""
        }
        return if (matcher.groupCount() == 0) {
            ""
        } else matcher.group(1)
    }

    private fun getDuplicatedIdFromPg(message: String): String {
        val matcher = PATTERN_POSTGRESQL.matcher(message)
        if (!matcher.find()) {
            return ""
        }
        return if (matcher.groupCount() == 0) {
            ""
        } else matcher.group(1)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JDBCEventStore::class.java)
        private const val INSERT_EVENT_SQL =
            "INSERT INTO %s (aggregate_root_id, aggregate_root_type_name, command_id, version, gmt_create, events) VALUES (?, ?, ?, ?, ?, ?)"
        private const val SELECT_MANY_BY_VERSION_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = ? AND version >= ? AND Version <= ? ORDER BY version"
        private const val SELECT_ONE_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version = ?"
        private const val SELECT_ONE_BY_COMMAND_ID_SQL =
            "SELECT * FROM %s WHERE aggregate_root_id = ? AND command_id = ?"

        private val PATTERN_POSTGRESQL = Pattern.compile("=\\(.*, (.*)\\) already exists.$")
        private val PATTERN_MYSQL = Pattern.compile("^Duplicate entry '.*-(.*)' for key")
    }

    init {
        if (DbType.MySQL.name == configuration.dbType) {
            this.code = "23000"
        }
        if (DbType.Pg.name == configuration.dbType) {
            this.code = "23505"
        }
    }
}
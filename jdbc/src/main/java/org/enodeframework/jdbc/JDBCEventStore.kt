package org.enodeframework.jdbc

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.sql.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
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
import java.util.stream.Collectors
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
abstract class JDBCEventStore(dataSource: DataSource, dbConfiguration: DBConfiguration, eventSerializer: IEventSerializer, private val serializeService: ISerializeService) : CoroutineVerticle(), IEventStore, SQLDialect {
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

    private fun batchAppendAggregateEventsAsync(aggregateRootId: String, eventStreamList: List<DomainEventStream>, batchAggregateEventAppendResult: BatchAggregateEventAppendResult, retryTimes: Int) {
        IOHelper.tryAsyncActionRecursively("BatchAppendAggregateEventsAsync", {
            CoroutineScope(Dispatchers.Default)
                    .async { batchAppendAggregateEvents(aggregateRootId, eventStreamList) }
                    .asCompletableFuture()
        }, { result: AggregateEventAppendResult -> batchAggregateEventAppendResult.addCompleteAggregate(aggregateRootId, result) }, {
            String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size)
        }, null, retryTimes, true)
    }

    private suspend fun batchAppendAggregateEvents(aggregateRootId: String, eventStreamList: List<DomainEventStream>): AggregateEventAppendResult {
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
        var conn: SQLConnection? = null
        try {
            conn = sqlClient.getConnectionAwait()
            conn.setAutoCommitAwait(false)
            conn.batchWithParamsAwait(sql, jsonArrays)
            conn.commitAwait()
            conn.closeAwait()
            val appendResult = AggregateEventAppendResult()
            appendResult.eventAppendStatus = EventAppendStatus.Success
            return appendResult
        } catch (throwable: Throwable) {
            try {
                conn?.rollbackAwait()
                conn?.closeAwait()
            } catch (e: Throwable) {
                // ignore
                logger.error("connection loss when transaction rollback, {}", jsonArrays, throwable)
            }
            if (throwable is SQLException) {
                if (sqlState == throwable.sqlState && throwable.message!!.contains(versionIndexName)) {
                    val appendResult = AggregateEventAppendResult()
                    appendResult.eventAppendStatus = EventAppendStatus.DuplicateEvent
                    return appendResult
                }
                if (sqlState == throwable.sqlState && throwable.message!!.contains(commandIndexName)) {
                    // 不同的数据库在冲突时的错误信息不同，可以通过解析错误信息的方式将冲突的commandId找出来，这里要求id不能命中正则的规则
                    // MySQL: ** Duplicate entry '5d3ac841d1fcfe669e9a257d-5d3ac841d1fcfe669e9a2585' for key 'IX_EventStream_AggId_CommandId' **
                    // PG: ** ERROR: duplicate key value violates unique constraint "event_stream_aggregate_root_id_command_id_key"\n详细：Key (aggregate_root_id, command_id)=(5ee99656d767113d73a7540f, 5ee99656d767113d73a75417) already exists. **
                    val appendResult = AggregateEventAppendResult()
                    appendResult.eventAppendStatus = EventAppendStatus.DuplicateCommand
                    val commandId = getDuplicatedId(throwable)
                    if (!Strings.isNullOrEmpty(commandId)) {
                        appendResult.duplicateCommandIds = Lists.newArrayList(commandId)
                        return appendResult
                    }
                    // 如果没有从异常信息获取到commandId(很低概率获取不到)，需要从db查询出来
                    // 但是Vert.x的线程模型决定了不能再次使用EventLoop线程执行阻塞的查询操作
                    return appendResult
                }
                logger.error("Batch append event has sql exception.", throwable)
                throw IORuntimeException(throwable)
            }
            logger.error("Batch append event has unknown exception.", throwable)
            throw EventStoreException(throwable)
        }
    }

    override fun queryAggregateEventsAsync(aggregateRootId: String, aggregateRootTypeName: String, minVersion: Int, maxVersion: Int): CompletableFuture<List<DomainEventStream>> {
        return IOHelper.tryIOFuncAsync({
            CoroutineScope(Dispatchers.Default).async {
                queryAggregateEvents(aggregateRootId, aggregateRootTypeName, minVersion, maxVersion)
            }.asCompletableFuture()
        }, "QueryAggregateEventsAsync")
    }

    private suspend fun queryAggregateEvents(aggregateRootId: String, aggregateRootTypeName: String, minVersion: Int, maxVersion: Int): List<DomainEventStream> {
        val sql = String.format(SELECT_MANY_BY_VERSION_SQL, tableName)
        val array = JsonArray()
        array.add(aggregateRootId)
        array.add(minVersion)
        array.add(maxVersion)
        try {
            val resultSet = sqlClient.queryWithParamsAwait(sql, array)
            return resultSet.rows
                    .map { row: JsonObject -> convertFrom(row.mapTo(StreamRecord::class.java)) }
                    .toMutableList()
        } catch (throwable: Throwable) {
            if (throwable is SQLException) {
                val errorMessage = String.format("Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s", aggregateRootId, aggregateRootTypeName)
                logger.error(errorMessage, throwable)
                throw IORuntimeException(throwable)
            }
            logger.error("Failed to query aggregate events async, aggregateRootId: {}, aggregateRootType: {}", aggregateRootId, aggregateRootTypeName, throwable)
            throw EventStoreException(throwable)
        }
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            CoroutineScope(Dispatchers.Default).async {
                findByVersion(aggregateRootId, version)
            }.asCompletableFuture()
        }, "FindEventByVersionAsync")
    }

    private suspend fun findByVersion(aggregateRootId: String, version: Int): DomainEventStream? {
        val sql = String.format(SELECT_ONE_BY_VERSION_SQL, tableName)
        val array = JsonArray()
        array.add(aggregateRootId)
        array.add(version)
        try {
            val resultSet = sqlClient.queryWithParamsAwait(sql, array)
            return resultSet.rows.firstOrNull()?.mapTo(StreamRecord::class.java)?.let { convertFrom(it) }
        } catch (throwable: Throwable) {
            if (throwable is SQLException) {
                logger.error("Find event by version has sql exception, aggregateRootId: {}, version: {}", aggregateRootId, version, throwable)
                throw IORuntimeException(throwable)
            }
            logger.error("Find event by version has unknown exception, aggregateRootId: {}, version: {}", aggregateRootId, version, throwable)
            throw EventStoreException(throwable)
        }
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return IOHelper.tryIOFuncAsync({
            CoroutineScope(Dispatchers.Default).async {
                findByCommandId(aggregateRootId, commandId)
            }.asCompletableFuture()
        }, "FindEventByCommandIdAsync")
    }

    private suspend fun findByCommandId(aggregateRootId: String, commandId: String): DomainEventStream? {
        val sql = String.format(SELECT_ONE_BY_COMMAND_ID_SQL, tableName)
        val array = JsonArray()
        array.add(aggregateRootId)
        array.add(commandId)
        try {
            val resultSet = sqlClient.queryWithParamsAwait(sql, array)
            return resultSet.rows.firstOrNull()?.mapTo(StreamRecord::class.java)?.let { convertFrom(it) }
        } catch (throwable: Throwable) {
            if (throwable is SQLException) {
                logger.error("Find event by commandId has sql exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, throwable)
                throw IORuntimeException(throwable)
            }
            logger.error("Find event by commandId has unknown exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, throwable)
            throw EventStoreException(throwable)
        }
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

    companion object {
        private val logger = LoggerFactory.getLogger(JDBCEventStore::class.java)
        private const val INSERT_EVENT_SQL = "INSERT INTO %s (aggregate_root_id, aggregate_root_type_name, command_id, version, gmt_create, events) VALUES (?, ?, ?, ?, ?, ?)"
        private const val SELECT_MANY_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version >= ? AND Version <= ? ORDER BY version"
        private const val SELECT_ONE_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version = ?"
        private const val SELECT_ONE_BY_COMMAND_ID_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND command_id = ?"
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
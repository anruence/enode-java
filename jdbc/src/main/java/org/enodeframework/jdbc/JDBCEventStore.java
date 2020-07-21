package org.enodeframework.jdbc;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import org.enodeframework.common.exception.EventStoreException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.eventing.AggregateEventAppendResult;
import org.enodeframework.eventing.BatchAggregateEventAppendResult;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventAppendResult;
import org.enodeframework.eventing.EventAppendStatus;
import org.enodeframework.eventing.IDomainEvent;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.eventing.IEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public abstract class JDBCEventStore extends AbstractVerticle implements IEventStore {

    private static final Logger logger = LoggerFactory.getLogger(JDBCEventStore.class);

    private static final String EVENT_TABLE_NAME_FORMAT = "%s_%s";

    private static final String INSERT_EVENT_SQL = "INSERT INTO %s (aggregate_root_id, aggregate_root_type_name, command_id, version, gmt_create, events) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_MANY_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version >= ? AND Version <= ? ORDER BY version";
    private static final String SELECT_ONE_BY_VERSION_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND version = ?";
    private static final String SELECT_ONE_BY_COMMAND_ID_SQL = "SELECT * FROM %s WHERE aggregate_root_id = ? AND command_id = ?";

    private final String tableName;
    private final int tableCount;
    private final String sqlState;
    private final String versionIndexName;
    private final String commandIndexName;
    /**
     * dataSource 如果使用了分库分表的ShardDataSource，分表则不再需要
     */
    private final DataSource dataSource;
    private final IEventSerializer eventSerializer;
    private final ISerializeService serializeService;
    private SQLClient sqlClient;

    public JDBCEventStore(DataSource dataSource, IEventSerializer eventSerializer, ISerializeService serializeService) {
        this(dataSource, new DBConfiguration(), eventSerializer, serializeService);
    }

    public JDBCEventStore(DataSource dataSource, DBConfiguration dbConfiguration, IEventSerializer eventSerializer, ISerializeService serializeService) {
        this.serializeService = serializeService;
        Ensure.notNull(dataSource, "dataSource");
        Ensure.notNull(eventSerializer, "eventSerializer");
        Ensure.notNull(dbConfiguration, "dbConfiguration");
        this.eventSerializer = eventSerializer;
        this.dataSource = dataSource;
        this.tableName = dbConfiguration.getEventTableName();
        this.tableCount = dbConfiguration.getEventTableCount();
        this.sqlState = dbConfiguration.getSqlState();
        this.versionIndexName = dbConfiguration.getEventTableVersionUniqueIndexName();
        this.commandIndexName = dbConfiguration.getEventTableCommandIdUniqueIndexName();
    }

    @Override
    public void start() {
        sqlClient = JDBCClient.create(vertx, dataSource);
    }

    @Override
    public CompletableFuture<EventAppendResult> batchAppendAsync(List<DomainEventStream> eventStreams) {
        CompletableFuture<EventAppendResult> future = new CompletableFuture<>();
        EventAppendResult appendResult = new EventAppendResult();
        if (eventStreams.size() == 0) {
            future.complete(appendResult);
            return future;
        }
        Map<String, List<DomainEventStream>> eventStreamMap = eventStreams.stream().distinct().collect(Collectors.groupingBy(DomainEventStream::getAggregateRootId));
        BatchAggregateEventAppendResult batchAggregateEventAppendResult = new BatchAggregateEventAppendResult(eventStreamMap.keySet().size());
        for (Map.Entry<String, List<DomainEventStream>> entry : eventStreamMap.entrySet()) {
            batchAppendAggregateEventsAsync(entry.getKey(), entry.getValue(), batchAggregateEventAppendResult, 0);
        }
        return batchAggregateEventAppendResult.taskCompletionSource;
    }

    private void batchAppendAggregateEventsAsync(String aggregateRootId, List<DomainEventStream> eventStreamList, BatchAggregateEventAppendResult batchAggregateEventAppendResult, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("BatchAppendAggregateEventsAsync",
                () -> batchAppendAggregateEventsAsync(aggregateRootId, eventStreamList),
                result -> {
                    batchAggregateEventAppendResult.addCompleteAggregate(aggregateRootId, result);
                },
                () -> String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size()),
                null,
                retryTimes, true);
    }

    private CompletableFuture<DomainEventStream> tryFindEventByCommandIdAsync(String aggregateRootId, String commandId, List<String> duplicateCommandIds, int retryTimes) {
        CompletableFuture<DomainEventStream> future = new CompletableFuture<>();
        IOHelper.tryAsyncActionRecursively("TryFindEventByCommandIdAsync",
                () -> findAsync(aggregateRootId, commandId),
                result -> {
                    if (result != null) {
                        duplicateCommandIds.add(result.getCommandId());
                    }
                    future.complete(result);
                },
                () -> String.format("[aggregateRootId:%s, commandId:%s]", aggregateRootId, commandId),
                null,
                retryTimes, true);
        return future;
    }

    private CompletableFuture<AggregateEventAppendResult> batchAppendAggregateEventsAsync(String aggregateRootId, List<DomainEventStream> eventStreamList) {
        CompletableFuture<AggregateEventAppendResult> future = new CompletableFuture<>();
        String sql = String.format(INSERT_EVENT_SQL, getTableName(aggregateRootId));
        List<JsonArray> jsonArrays = Lists.newArrayList();
        eventStreamList.sort(Comparator.comparingInt(DomainEventStream::getVersion));
        for (DomainEventStream domainEventStream : eventStreamList) {
            JsonArray array = new JsonArray();
            array.add(domainEventStream.getAggregateRootId());
            array.add(domainEventStream.getAggregateRootTypeName());
            array.add(domainEventStream.getCommandId());
            array.add(domainEventStream.getVersion());
            array.add(domainEventStream.getTimestamp().toInstant());
            array.add(serializeService.serialize(eventSerializer.serialize(domainEventStream.events())));
            jsonArrays.add(array);
        }
        if (jsonArrays.size() > 1) {
            future = batchInsertAsync(sql, jsonArrays);
        } else {
            future = insertOneByOneAsync(sql, jsonArrays);
        }
        return future.exceptionally(throwable -> {
            if (throwable instanceof SQLException) {
                SQLException ex = (SQLException) throwable;
                if (ex.getSQLState().equals(sqlState) && ex.getMessage().contains(versionIndexName)) {
                    AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                    appendResult.setEventAppendStatus(EventAppendStatus.DuplicateEvent);
                    return appendResult;
                }
                if (ex.getSQLState().equals(sqlState) && ex.getMessage().contains(commandIndexName)) {
                    // Duplicate entry '5d3ac841d1fcfe669e9a257d-5d3ac841d1fcfe669e9a2585' for key 'IX_EventStream_AggId_CommandId'
                    // ERROR: duplicate key value violates unique constraint "event_stream_aggregate_root_id_command_id_key"\n详细：Key (aggregate_root_id, command_id)=(5ee99656d767113d73a7540f, 5ee99656d767113d73a75417) already exists.
                    AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                    appendResult.setEventAppendStatus(EventAppendStatus.DuplicateCommand);
                    String commandId = parseDuplicateCommandId(ex.getMessage());
                    if (!Strings.isNullOrEmpty(commandId)) {
                        appendResult.setDuplicateCommandIds(Lists.newArrayList(commandId));
                        return appendResult;
                    }
                    // 如果没有从异常信息获取到commandId，兜底从db查询出来，此时选择的策略是从最后一个开始查
                    // 但是Vert.x的线程模型决定了不能再次使用eventloop线程执行阻塞的查询操作
                    // 这里使用了executeBlocking执行同步任务，但没有阻塞获取
                    for (int i = eventStreamList.size() - 1; i >= 0; i--) {
                        CompletableFuture<Boolean> block = new CompletableFuture<>();
                        DomainEventStream eventStream = eventStreamList.get(i);
                        vertx.executeBlocking(promise -> {
                            DomainEventStream dbEventStream = Task.await(tryFindEventByCommandIdAsync(aggregateRootId, eventStream.getCommandId(), appendResult.getDuplicateCommandIds(), 0));
                            promise.complete(dbEventStream);
                        }, res -> {
                            block.complete(res != null);
                        });
                        if (Task.await(block)) {
                            break;
                        }
                    }
                    return appendResult;
                }
                logger.error("Batch append event has sql exception.", throwable);
                throw new IORuntimeException(throwable);
            }
            logger.error("Batch append event has unknown exception.", throwable);
            throw new EventStoreException(throwable);
        });
    }

    abstract protected String parseDuplicateCommandId(String msg);

    public CompletableFuture<AggregateEventAppendResult> insertOneByOneAsync(String sql, List<JsonArray> jsonArrays) {
        CompletableFuture<AggregateEventAppendResult> future = new CompletableFuture<>();
        CountDownLatch latch = new CountDownLatch(jsonArrays.size());
        for (JsonArray array : jsonArrays) {
            sqlClient.updateWithParams(sql, array, x -> {
                if (x.succeeded()) {
                    latch.countDown();
                    if (latch.getCount() == 0) {
                        AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                        appendResult.setEventAppendStatus(EventAppendStatus.Success);
                        future.complete(appendResult);
                    }
                    return;
                }
                latch.countDown();
                future.completeExceptionally(x.cause());
            });
            if (future.isDone()) {
                break;
            }
        }
        return future;
    }

    public CompletableFuture<AggregateEventAppendResult> batchInsertAsync(String sql, List<JsonArray> jsonArrays) {
        CompletableFuture<AggregateEventAppendResult> future = new CompletableFuture<>();
        batchWithParams(sql, jsonArrays, x -> {
            if (x.succeeded()) {
                AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                appendResult.setEventAppendStatus(EventAppendStatus.Success);
                future.complete(appendResult);
                return;
            }
            future.completeExceptionally(x.cause());
        });
        return future;
    }

    @Override
    public CompletableFuture<List<DomainEventStream>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion) {
        return IOHelper.tryIOFuncAsync(() -> {
            CompletableFuture<List<DomainEventStream>> future = new CompletableFuture<>();
            String sql = String.format(SELECT_MANY_BY_VERSION_SQL, getTableName(aggregateRootId));
            JsonArray array = new JsonArray();
            array.add(aggregateRootId);
            array.add(minVersion);
            array.add(maxVersion);
            sqlClient.queryWithParams(sql, array, x -> {
                if (x.succeeded()) {
                    List<StreamRecord> results = Lists.newArrayList();
                    x.result().getRows().forEach(row -> results.add(row.mapTo(StreamRecord.class)));
                    List<DomainEventStream> streams = results.stream().map(this::convertFrom).collect(Collectors.toList());
                    future.complete(streams);
                    return;
                }
                future.completeExceptionally(x.cause());
            });
            return future.exceptionally(throwable -> {
                if (throwable instanceof SQLException) {
                    SQLException ex = (SQLException) throwable;
                    String errorMessage = String.format("Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s", aggregateRootId, aggregateRootTypeName);
                    logger.error(errorMessage, ex);
                    throw new IORuntimeException(throwable);
                }
                logger.error("Failed to query aggregate events async, aggregateRootId: {}, aggregateRootType: {}", aggregateRootId, aggregateRootTypeName, throwable);
                throw new EventStoreException(throwable);
            });
        }, "QueryAggregateEventsAsync");
    }

    @Override
    public CompletableFuture<DomainEventStream> findAsync(String aggregateRootId, int version) {
        return IOHelper.tryIOFuncAsync(() -> {
            CompletableFuture<DomainEventStream> future = new CompletableFuture<>();
            String sql = String.format(SELECT_ONE_BY_VERSION_SQL, getTableName(aggregateRootId));
            JsonArray array = new JsonArray();
            array.add(aggregateRootId);
            array.add(version);
            sqlClient.queryWithParams(sql, array, x -> {
                if (x.succeeded()) {
                    DomainEventStream stream = null;
                    Optional<JsonObject> first = x.result().getRows().stream().findFirst();
                    if (first.isPresent()) {
                        StreamRecord record = first.get().mapTo(StreamRecord.class);
                        stream = convertFrom(record);
                    }
                    future.complete(stream);
                    return;
                }
                future.completeExceptionally(x.cause());
            });
            return future.exceptionally(throwable -> {
                if (throwable instanceof SQLException) {
                    SQLException ex = (SQLException) throwable;
                    logger.error("Find event by version has sql exception, aggregateRootId: {}, version: {}", aggregateRootId, version, ex);
                    throw new IORuntimeException(throwable);
                }
                logger.error("Find event by version has unknown exception, aggregateRootId: {}, version: {}", aggregateRootId, version, throwable);
                throw new EventStoreException(throwable);
            });
        }, "FindEventByVersionAsync");

    }

    @Override
    public CompletableFuture<DomainEventStream> findAsync(String aggregateRootId, String commandId) {
        return IOHelper.tryIOFuncAsync(() -> {
            CompletableFuture<DomainEventStream> future = new CompletableFuture<>();
            String sql = String.format(SELECT_ONE_BY_COMMAND_ID_SQL, getTableName(aggregateRootId));
            JsonArray array = new JsonArray();
            array.add(aggregateRootId);
            array.add(commandId);
            sqlClient.queryWithParams(sql, array, x -> {
                if (x.succeeded()) {
                    DomainEventStream stream = null;
                    Optional<JsonObject> first = x.result().getRows().stream().findFirst();
                    if (first.isPresent()) {
                        StreamRecord record = first.get().mapTo(StreamRecord.class);
                        stream = convertFrom(record);
                    }
                    future.complete(stream);
                    return;
                }
                future.completeExceptionally(x.cause());
            });
            return future.exceptionally(throwable -> {
                if (throwable instanceof SQLException) {
                    SQLException ex = (SQLException) throwable;
                    logger.error("Find event by commandId has sql exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, ex);
                    throw new IORuntimeException(throwable);
                }
                logger.error("Find event by commandId has unknown exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, throwable);
                throw new EventStoreException(throwable);
            });
        }, "FindEventByCommandIdAsync");

    }

    private int getTableIndex(String aggregateRootId) {
        int hash = aggregateRootId.hashCode();
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash % tableCount;
    }

    private String getTableName(String aggregateRootId) {
        if (tableCount <= 1) {
            return tableName;
        }
        int tableIndex = getTableIndex(aggregateRootId);
        return String.format(EVENT_TABLE_NAME_FORMAT, tableName, tableIndex);
    }

    private DomainEventStream convertFrom(StreamRecord record) {
        return new DomainEventStream(
                record.commandId,
                record.aggregateRootId,
                record.aggregateRootTypeName,
                record.gmtCreated,
                eventSerializer.deserialize(serializeService.deserialize(record.events, Map.class), IDomainEvent.class),
                Maps.newHashMap());
    }

    private SQLClient batchWithParams(String sql, List<JsonArray> params, Handler<AsyncResult<List<Integer>>> handler) {
        sqlClient.getConnection(getConnection -> {
            if (getConnection.failed()) {
                handler.handle(Future.failedFuture(getConnection.cause()));
                return;
            }
            final SQLConnection conn = getConnection.result();
            conn.setAutoCommit(false, autocommit -> {
                if (autocommit.failed()) {
                    handler.handle(Future.failedFuture(autocommit.cause()));
                    resetAutoCommitAndCloseConnection(conn);
                    return;
                }
                conn.batchWithParams(sql, params, batch -> {
                    if (batch.succeeded()) {
                        conn.commit(commit -> {
                            if (commit.succeeded()) {
                                handler.handle(Future.succeededFuture(batch.result()));
                            } else {
                                handler.handle(Future.failedFuture(commit.cause()));
                            }
                            resetAutoCommitAndCloseConnection(conn);
                        });
                    } else {
                        conn.rollback(rollback -> {
                            if (rollback.succeeded()) {
                                handler.handle(Future.failedFuture(batch.cause()));
                            } else {
                                handler.handle(Future.failedFuture(rollback.cause()));
                            }
                            resetAutoCommitAndCloseConnection(conn);
                        });
                    }
                });
            });
        });
        return sqlClient;
    }

    private void resetAutoCommitAndCloseConnection(SQLConnection conn) {
        conn.setAutoCommit(true, commit -> {
            if (commit.failed()) {
                logger.error("jdbc driver set autocommit true failed", commit.cause());
            }
            // close will put the connection into pool
            conn.close();
        });
    }
}

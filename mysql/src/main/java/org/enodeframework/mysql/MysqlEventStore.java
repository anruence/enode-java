package org.enodeframework.mysql;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import org.enodeframework.ObjectContainer;
import org.enodeframework.common.exception.VertxRuntimeException;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.configurations.DataSourceKey;
import org.enodeframework.configurations.DefaultDBConfigurationSetting;
import org.enodeframework.configurations.OptionSetting;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventAppendResult;
import org.enodeframework.eventing.EventAppendStatus;
import org.enodeframework.eventing.IDomainEvent;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.eventing.IEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class MysqlEventStore implements IEventStore {
    private static final Logger logger = LoggerFactory.getLogger(MysqlEventStore.class);
    private static final String EVENT_TABLE_NAME_FORMAT = "%s_%s";
    private final String tableName;
    private final int tableCount;
    private final String versionIndexName;
    private final String commandIndexName;
    private final int bulkCopyBatchSize;
    private final int bulkCopyTimeout;
    private final SQLClient sqlClient;

    private final String INSERT_EVENT_SQL = "INSERT INTO %s(AggregateRootId,AggregateRootTypeName,CommandId,Version,CreatedOn,Events) VALUES(?,?,?,?,?,?)";

    @Autowired
    private IEventSerializer eventSerializer;

    public MysqlEventStore(DataSource ds, OptionSetting optionSetting) {
        Ensure.notNull(ds, "ds");
        if (optionSetting != null) {
            tableName = optionSetting.getOptionValue(DataSourceKey.EVENT_TABLE_NAME);
            tableCount = Integer.parseInt(optionSetting.getOptionValue(DataSourceKey.EVENT_TABLE_COUNT));
            versionIndexName = optionSetting.getOptionValue(DataSourceKey.EVENT_TABLE_VERSION_UNIQUE_INDEX_NAME);
            commandIndexName = optionSetting.getOptionValue(DataSourceKey.COMMAND_TABLE_COMMANDID_UNIQUE_INDEX_NAME);
            bulkCopyBatchSize = Integer.parseInt(optionSetting.getOptionValue(DataSourceKey.EVENT_TABLE_BULKCOPY_BATCHSIZE));
            bulkCopyTimeout = Integer.parseInt(optionSetting.getOptionValue(DataSourceKey.EVENT_TABLE_BULKCOPY_TIMEOUT));
        } else {
            DefaultDBConfigurationSetting setting = new DefaultDBConfigurationSetting();
            tableName = setting.getEventTableName();
            tableCount = setting.getEventTableCount();
            versionIndexName = setting.getEventTableVersionUniqueIndexName();
            commandIndexName = setting.getEventTableCommandIdUniqueIndexName();
            bulkCopyBatchSize = setting.getEventTableBulkCopyBatchSize();
            bulkCopyTimeout = setting.getEventTableBulkCopyTimeout();
        }
        Ensure.notNull(tableName, "tableName");
        Ensure.notNull(versionIndexName, "eventIndexName");
        Ensure.notNull(commandIndexName, "commandIndexName");
        Ensure.positive(bulkCopyBatchSize, "bulkCopyBatchSize");
        Ensure.positive(bulkCopyTimeout, "bulkCopyTimeout");
        sqlClient = JDBCClient.create(ObjectContainer.vertx, ds);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<EventAppendResult>> batchAppendAsync(List<DomainEventStream> eventStreams) {
        CompletableFuture<AsyncTaskResult<EventAppendResult>> future = new CompletableFuture<>();
        EventAppendResult appendResult = new EventAppendResult();
        if (eventStreams.size() == 0) {
            future.complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, appendResult));
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
                    EventAppendStatus eventAppendStatus = result.getData();
                    if (eventAppendStatus == EventAppendStatus.Success) {
                        AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                        appendResult.setEventAppendStatus(EventAppendStatus.Success);
                        batchAggregateEventAppendResult.addCompleteAggregate(aggregateRootId, appendResult);
                    } else if (eventAppendStatus == EventAppendStatus.DuplicateEvent) {
                        AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                        appendResult.setEventAppendStatus(EventAppendStatus.DuplicateEvent);
                        batchAggregateEventAppendResult.addCompleteAggregate(aggregateRootId, appendResult);
                    } else if (eventAppendStatus == EventAppendStatus.DuplicateCommand) {
                        for (DomainEventStream eventStream : eventStreamList) {
                            tryFindEventByCommandIdAsync(aggregateRootId, eventStream.getCommandId(), batchAggregateEventAppendResult, 0);
                        }
                    }
                },
                () -> String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size()),
                errorMessage -> {
                    logger.error("BatchAppendAggregateEventsAsync has unknown exception, the code should not be run to here, aggregateRootId: {}, errorMessage: {}", aggregateRootId, errorMessage);
                },
                retryTimes, true);
    }

    private void tryFindEventByCommandIdAsync(String aggregateRootId, String commandId, BatchAggregateEventAppendResult batchAggregateEventAppendResult, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("TryFindEventByCommandIdAsync",
                () -> findAsync(aggregateRootId, commandId),
                result ->
                {
                    if (result.getData() != null) {
                        AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                        appendResult.setEventAppendStatus(EventAppendStatus.DuplicateCommand);
                        appendResult.setDuplicateCommandId(result.getData().getCommandId());
                        batchAggregateEventAppendResult.addCompleteAggregate(aggregateRootId, appendResult);
                    }
                },
                () -> String.format("[aggregateRootId:%s, commandId:%s]", aggregateRootId, commandId),
                errorMessage ->
                {
                    logger.error("TryFindEventByCommandIdAsync has unknown exception, the code should not be run to here, aggregateRootId:{}, commandId:{}, errorMessage: {}", aggregateRootId, commandId, errorMessage);
                },
                retryTimes, true);
    }

    private CompletableFuture<AsyncTaskResult<EventAppendStatus>> batchAppendAggregateEventsAsync(String aggregateRootId, List<DomainEventStream> eventStreamList) {
        CompletableFuture<AsyncTaskResult<EventAppendStatus>> future = new CompletableFuture<>();
        String sql = String.format(INSERT_EVENT_SQL, getTableName(aggregateRootId));
        List<JsonArray> jsonArrays = Lists.newArrayList();
        for (DomainEventStream domainEventStream : eventStreamList) {
            JsonArray array = new JsonArray();
            array.add(domainEventStream.getAggregateRootId());
            array.add(domainEventStream.getAggregateRootTypeName());
            array.add(domainEventStream.getCommandId());
            array.add(domainEventStream.getVersion());
            array.add(domainEventStream.getTimestamp().toInstant());
            array.add(JsonTool.serialize(eventSerializer.serialize(domainEventStream.events())));
            jsonArrays.add(array);
        }
        batchWithParams(sql, jsonArrays, x -> {
            if (x.succeeded()) {
                future.complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, EventAppendStatus.Success));
                return;
            }
            future.completeExceptionally(x.cause());
        });
        return future.exceptionally(throwable -> {
            if (throwable instanceof SQLException) {
                SQLException ex = (SQLException) throwable;
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(versionIndexName)) {
                    return new AsyncTaskResult<>(AsyncTaskStatus.Success, EventAppendStatus.DuplicateEvent);
                }
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(commandIndexName)) {
                    return new AsyncTaskResult<>(AsyncTaskStatus.Success, EventAppendStatus.DuplicateCommand);
                }

                logger.error("Batch append event has sql exception.", throwable);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, throwable.getMessage());
            }
            logger.error("Batch append event has unknown exception.", throwable);
            return new AsyncTaskResult<>(AsyncTaskStatus.Failed, throwable.getMessage());
        });
    }


    @Override
    public CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion) {
        return IOHelper.tryIOFuncAsync(() -> {
            CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> future = new CompletableFuture<>();
            String sql = String.format("SELECT * FROM `%s` WHERE AggregateRootId = ? AND Version >= ? AND Version <= ? ORDER BY Version", getTableName(aggregateRootId));
            JsonArray array = new JsonArray();
            array.add(aggregateRootId);
            array.add(minVersion);
            array.add(maxVersion);
            sqlClient.queryWithParams(sql, array, x -> {
                if (x.succeeded()) {
                    List<StreamRecord> results = Lists.newArrayList();
                    x.result().getRows().forEach(row -> results.add(row.mapTo(StreamRecord.class)));
                    List<DomainEventStream> streams = results.stream().map(this::convertFrom).collect(Collectors.toList());
                    future.complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, streams));
                    return;
                }
                future.completeExceptionally(x.cause());
            });
            return future.exceptionally(x -> {
                if (x instanceof SQLException) {
                    SQLException ex = (SQLException) x;
                    String errorMessage = String.format("Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s", aggregateRootId, aggregateRootTypeName);
                    logger.error(errorMessage, ex);
                    return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
                }
                logger.error("Failed to query aggregate events async, aggregateRootId: {}, aggregateRootType: {}", aggregateRootId, aggregateRootTypeName, x);
                return new AsyncTaskResult<>(AsyncTaskStatus.Failed, x.getMessage());
            });
        }, "QueryAggregateEventsAsync");
    }

    @Override
    public CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, int version) {
        return IOHelper.tryIOFuncAsync(() -> {
            CompletableFuture<AsyncTaskResult<DomainEventStream>> future = new CompletableFuture<>();
            String sql = String.format("select * from `%s` where AggregateRootId=? and Version=?", getTableName(aggregateRootId));
            JsonArray array = new JsonArray();
            array.add(aggregateRootId);
            array.add(version);
            sqlClient.queryWithParams(sql, array, x -> {
                if (x.succeeded()) {
                    StreamRecord record = null;
                    if (x.result().getRows().size() >= 1) {
                        record = x.result().getRows().get(0).mapTo(StreamRecord.class);
                    }
                    DomainEventStream stream = record != null ? convertFrom(record) : null;
                    future.complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, stream));
                    return;
                }
                future.completeExceptionally(x.cause());
            });
            return future.exceptionally(throwable -> {
                if (throwable instanceof SQLException) {
                    SQLException ex = (SQLException) throwable;
                    logger.error("Find event by version has sql exception, aggregateRootId: {}, version: {}", aggregateRootId, version, ex);
                    return (new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage()));
                }
                logger.error("Find event by version has unknown exception, aggregateRootId: {}, version: {}", aggregateRootId, version, throwable);
                return (new AsyncTaskResult<>(AsyncTaskStatus.Failed, throwable.getMessage()));
            });
        }, "FindEventByVersionAsync");

    }

    @Override
    public CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, String commandId) {
        return IOHelper.tryIOFuncAsync(() -> {
            CompletableFuture<AsyncTaskResult<DomainEventStream>> future = new CompletableFuture<>();
            String sql = String.format("select * from `%s` where AggregateRootId=? and CommandId=?", getTableName(aggregateRootId));
            JsonArray array = new JsonArray();
            array.add(aggregateRootId);
            array.add(commandId);
            sqlClient.queryWithParams(sql, array, x -> {
                if (x.succeeded()) {
                    StreamRecord record = null;
                    if (x.result().getRows().size() >= 1) {
                        record = x.result().getRows().get(0).mapTo(StreamRecord.class);
                    }
                    DomainEventStream stream = record != null ? convertFrom(record) : null;
                    future.complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, stream));
                    return;
                }
                future.completeExceptionally(x.cause());
            });
            return future.exceptionally(throwable -> {
                if (throwable instanceof SQLException) {
                    SQLException ex = (SQLException) throwable;
                    logger.error("Find event by commandId has sql exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, ex);
                    return (new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage()));
                }
                logger.error("Find event by commandId has unknown exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, throwable);
                return (new AsyncTaskResult<>(AsyncTaskStatus.Failed, throwable.getMessage()));
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
                record.CommandId,
                record.AggregateRootId,
                record.AggregateRootTypeName,
                record.CreatedOn,
                eventSerializer.deserialize(JsonTool.deserialize(record.Events, Map.class), IDomainEvent.class),
                Maps.newHashMap());
    }

    private SQLClient batchWithParams(String sql, List<JsonArray> params, Handler<AsyncResult<List<Integer>>> handler) {
        sqlClient.getConnection(getConnection -> {
            if (getConnection.failed()) {
                handler.handle(Future.failedFuture(getConnection.cause()));
            } else {
                final SQLConnection conn = getConnection.result();
                startTx(conn, begin -> conn.batchWithParams(sql, params, batch -> {
                    if (batch.succeeded()) {
                        endTx(conn, end -> {
                            close(conn);
                            handler.handle(Future.succeededFuture(batch.result()));
                        });
                    } else {
                        rollbackTx(conn, rollback -> {
                            close(conn);
                            handler.handle(Future.failedFuture(batch.cause()));
                        });
                    }
                }));
            }
        });
        return sqlClient;
    }

    <T> void startTx(SQLConnection conn, Handler<T> done) {
        conn.setAutoCommit(false, res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            done.handle(null);
        });
    }

    <T> void endTx(SQLConnection conn, Handler<T> done) {
        conn.commit(res -> {
            if (res.failed()) {
                throw new VertxRuntimeException(res.cause());
            }
            done.handle(null);
        });
    }

    <T> void rollbackTx(SQLConnection conn, Handler<T> done) {
        conn.rollback(res -> {
            if (res.failed()) {
                throw new VertxRuntimeException(res.cause());
            }
            done.handle(null);
        });
    }

    <T> void close(SQLConnection conn) {
        // and close the connection
        conn.close(done -> {
            if (done.failed()) {
                throw new VertxRuntimeException(done.cause());
            }
        });
    }
}

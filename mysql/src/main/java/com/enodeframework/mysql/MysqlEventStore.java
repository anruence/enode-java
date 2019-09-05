package com.enodeframework.mysql;

import com.enodeframework.ObjectContainer;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.IOHelper;
import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.common.utilities.Linq;
import com.enodeframework.configurations.DataSourceKey;
import com.enodeframework.configurations.DefaultDBConfigurationSetting;
import com.enodeframework.configurations.OptionSetting;
import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.EventAppendResult;
import com.enodeframework.eventing.IDomainEvent;
import com.enodeframework.eventing.IEventSerializer;
import com.enodeframework.eventing.IEventStore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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
            throw new IllegalArgumentException("Event streams cannot be empty.");
        }
        Map<String, List<DomainEventStream>> eventStreamMap = eventStreams.stream().collect(Collectors.groupingBy(DomainEventStream::getAggregateRootId));
        CountDownLatch latch = new CountDownLatch(eventStreamMap.size());
        for (Map.Entry<String, List<DomainEventStream>> entry : eventStreamMap.entrySet()) {
            List<DomainEventStream> domainEventStreams = entry.getValue();
            String aggregateRootId = Linq.first(domainEventStreams).getAggregateRootId();
            String sql = String.format("INSERT INTO %s(AggregateRootId,AggregateRootTypeName,CommandId,Version,CreatedOn,Events) VALUES(?,?,?,?,?,?)", getTableName(aggregateRootId));
            int size = domainEventStreams.size();
            JsonArray array = new JsonArray();
            List<String> commandIds = Lists.newArrayList();
            for (int i = 0; i < size; i++) {
                DomainEventStream domainEventStream = domainEventStreams.get(i);
                array.add(domainEventStream.getAggregateRootId());
                array.add(domainEventStream.getAggregateRootTypeName());
                array.add(domainEventStream.getCommandId());
                array.add(domainEventStream.getVersion());
                array.add(domainEventStream.getTimestamp().toInstant());
                array.add(JsonTool.serialize(eventSerializer.serialize(domainEventStream.events())));
                commandIds.add("-" + domainEventStream.getCommandId());
                if (i == 0) {
                    continue;
                }
                sql = sql.concat(",(?,?,?,?,?,?)");
            }
            sqlClient.updateWithParams(sql, array, x -> {
                latch.countDown();
                if (x.succeeded()) {
                    appendResult.getSuccessAggregateRootIdList().add(entry.getKey());
                    if (latch.getCount() == 0) {
                        future.complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, appendResult));
                    }
                    return;
                }
                if (!(x.cause() instanceof SQLException)) {
                    future.completeExceptionally(x.cause());
                    return;
                }
                SQLException ex = (SQLException) x.cause();
                // Duplicate entry '5d3ac841d1fcfe669e9a257d-5d3ac841d1fcfe669e9a2585' for key 'IX_EventStream_AggId_CommandId'
                if (ex.getErrorCode() != 1062) {
                    future.completeExceptionally(ex);
                    return;
                }
                String exMessage = ex.getMessage();
                if (exMessage.contains(versionIndexName)) {
                    appendResult.getDuplicateEventAggregateRootIdList().add(aggregateRootId);
                } else if (exMessage.contains(commandIndexName)) {
                    for (String commandId : commandIds) {
                        if (exMessage.contains(commandId)) {
                            appendResult.getDuplicateCommandIdList().add(commandId.substring(1));
                        }
                    }
                }
                if (latch.getCount() == 0) {
                    future.complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, appendResult));
                }
            });

        }
        return future.exceptionally(throwable -> {
            if (throwable instanceof SQLException) {
                logger.error("Batch append event has sql exception.", throwable);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, throwable.getMessage(), appendResult);
            }
            logger.error("Batch append event has unknown exception.", throwable);
            return new AsyncTaskResult<>(AsyncTaskStatus.Failed, throwable.getMessage(), appendResult);
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
                record.Version,
                record.CreatedOn,
                eventSerializer.deserialize(JsonTool.deserialize(record.Events, Map.class), IDomainEvent.class),
                Maps.newHashMap());
    }
}

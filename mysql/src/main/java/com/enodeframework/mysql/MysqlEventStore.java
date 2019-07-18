package com.enodeframework.mysql;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.IOHelper;
import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.configurations.DefaultDBConfigurationSetting;
import com.enodeframework.configurations.OptionSetting;
import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.EventAppendResult;
import com.enodeframework.eventing.IDomainEvent;
import com.enodeframework.eventing.IEventSerializer;
import com.enodeframework.eventing.IEventStore;
import com.enodeframework.eventing.impl.StreamRecord;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
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
    private final QueryRunner queryRunner;

    @Autowired
    private IEventSerializer eventSerializer;

    private boolean supportBatchAppendEvent = true;

    public MysqlEventStore(DataSource ds, OptionSetting optionSetting) {
        Ensure.notNull(ds, "ds");
        if (optionSetting != null) {
            tableName = optionSetting.getOptionValue("TableName");
            tableCount = optionSetting.getOptionValue("TableCount") == null ? 1 : Integer.valueOf(optionSetting.getOptionValue("TableCount"));
            versionIndexName = optionSetting.getOptionValue("VersionIndexName");
            commandIndexName = optionSetting.getOptionValue("CommandIndexName");
            bulkCopyBatchSize = optionSetting.getOptionValue("BulkCopyBatchSize") == null ? 0 : Integer.valueOf(optionSetting.getOptionValue("BulkCopyBatchSize"));
            bulkCopyTimeout = optionSetting.getOptionValue("BulkCopyTimeout") == null ? 0 : Integer.valueOf(optionSetting.getOptionValue("BulkCopyTimeout"));
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
        queryRunner = new QueryRunner(ds);
    }

    @Override
    public boolean isSupportBatchAppendEvent() {
        return supportBatchAppendEvent;
    }

    public void setSupportBatchAppendEvent(boolean supportBatchAppendEvent) {
        this.supportBatchAppendEvent = supportBatchAppendEvent;
    }

    @Override
    public CompletableFuture<AsyncTaskResult<EventAppendResult>> batchAppendAsync(List<DomainEventStream> eventStreams) {
        return CompletableFuture.supplyAsync(() -> batchAppend(eventStreams));
    }

    @Override
    public CompletableFuture<AsyncTaskResult<EventAppendResult>> appendAsync(DomainEventStream eventStream) {
        return CompletableFuture.supplyAsync(() -> append(eventStream));
    }

    @Override
    public CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion) {
        return IOHelper.tryIOFuncAsync(() ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        String sql = String.format("SELECT * FROM `%s` WHERE AggregateRootId = ? AND Version >= ? AND Version <= ? ORDER BY Version", getTableName(aggregateRootId));
                        List<StreamRecord> result = queryRunner.query(sql,
                                new BeanListHandler<>(StreamRecord.class),
                                aggregateRootId,
                                minVersion,
                                maxVersion);

                        List<DomainEventStream> streams = result.stream().map(this::convertFrom).collect(Collectors.toList());
                        return new AsyncTaskResult<>(AsyncTaskStatus.Success, streams);
                    } catch (SQLException ex) {
                        String errorMessage = String.format("Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s", aggregateRootId, aggregateRootTypeName);
                        logger.error(errorMessage, ex);
                        return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
                    } catch (Exception ex) {
                        String errorMessage = String.format("Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s", aggregateRootId, aggregateRootTypeName);
                        logger.error(errorMessage, ex);
                        return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
                    }
                }), "QueryAggregateEventsAsync");
    }

    public AsyncTaskResult<EventAppendResult> batchAppend(List<DomainEventStream> eventStreams) {
        if (eventStreams.size() == 0) {
            throw new IllegalArgumentException("Event streams cannot be empty.");
        }

        Map<String, List<DomainEventStream>> eventStreamMap = eventStreams.stream().collect(Collectors.groupingBy(DomainEventStream::getAggregateRootId));
        for (List<DomainEventStream> x : eventStreamMap.values()) {
            if (x.size() > 1) {
                throw new IllegalArgumentException("Batch append event only support for one aggregate.");
            }
            try {
                Object[][] params = new Object[x.size()][];
                for (int i = 0, len = x.size(); i < len; i++) {
                    DomainEventStream eventStream = x.get(i);
                    params[i] = new Object[]{eventStream.getAggregateRootId(), eventStream.getAggregateRootTypeName(), eventStream.getCommandId(), eventStream.getVersion(), eventStream.getTimestamp(),
                            JsonTool.serialize(eventSerializer.serialize(eventStream.events()))};
                }
                String aggregateRootId = x.get(0).getAggregateRootId();
                queryRunner.batch(String.format("INSERT INTO %s(AggregateRootId,AggregateRootTypeName,CommandId,Version,CreatedOn,Events) VALUES(?,?,?,?,?,?)", getTableName(aggregateRootId)), params);
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(versionIndexName)) {
                    return new AsyncTaskResult<>(AsyncTaskStatus.Success, EventAppendResult.DuplicateEvent);
                } else if (ex.getErrorCode() == 1062 && ex.getMessage().contains(commandIndexName)) {
                    return new AsyncTaskResult<>(AsyncTaskStatus.Success, EventAppendResult.DuplicateCommand);
                }
                logger.error("Batch append event has sql exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage(), EventAppendResult.Failed);
            } catch (Exception ex) {
                logger.error("Batch append event has unknown exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage(), EventAppendResult.Failed);
            }
        }
        return new AsyncTaskResult<>(AsyncTaskStatus.Success, EventAppendResult.Success);
    }

    public AsyncTaskResult<EventAppendResult> append(DomainEventStream eventStream) {
        return IOHelper.tryIOFunc(() -> doAppend(eventStream), "AppendEvents");
    }

    private AsyncTaskResult<EventAppendResult> doAppend(final DomainEventStream eventStream) {
        StreamRecord record = convertTo(eventStream);
        try {
            queryRunner.update(String.format("INSERT INTO %s(AggregateRootId,AggregateRootTypeName,CommandId,Version,CreatedOn,Events) VALUES(?,?,?,?,?,?)", getTableName(record.getAggregateRootId())),
                    record.getAggregateRootId(),
                    record.getAggregateRootTypeName(),
                    record.getCommandId(),
                    record.getVersion(),
                    record.getCreatedOn(),
                    record.getEvents());
            return new AsyncTaskResult<>(AsyncTaskStatus.Success, EventAppendResult.Success);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062 && ex.getMessage().contains(versionIndexName)) {
                return new AsyncTaskResult<>(AsyncTaskStatus.Success, EventAppendResult.DuplicateEvent);
            } else if (ex.getErrorCode() == 1062 && ex.getMessage().contains(commandIndexName)) {
                return new AsyncTaskResult<>(AsyncTaskStatus.Success, EventAppendResult.DuplicateCommand);
            }

            logger.error(String.format("Append event has sql exception, eventStream: %s", eventStream), ex);
            return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage(), EventAppendResult.Failed);
        } catch (Exception ex) {
            logger.error(String.format("Append event has unknown exception, eventStream: %s", eventStream), ex);
            return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage(), EventAppendResult.Failed);
        }
    }

    @Override
    public CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, int version) {
        return IOHelper.tryIOFuncAsync(() ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        StreamRecord record = queryRunner.query(String.format("select * from `%s` where AggregateRootId=? and Version=?", getTableName(aggregateRootId)),
                                new BeanHandler<>(StreamRecord.class),
                                aggregateRootId,
                                version);

                        DomainEventStream stream = record != null ? convertFrom(record) : null;

                        return new AsyncTaskResult<>(AsyncTaskStatus.Success, stream);
                    } catch (SQLException ex) {
                        logger.error(String.format("Find event by version has sql exception, aggregateRootId: %s, version: %d", aggregateRootId, version), ex);
                        return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
                    } catch (Exception ex) {
                        logger.error(String.format("Find event by version has unknown exception, aggregateRootId: %s, version: %d", aggregateRootId, version), ex);
                        return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
                    }
                }), "FindEventByVersionAsync");
    }

    @Override
    public CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, String commandId) {
        return IOHelper.tryIOFuncAsync(() ->
                CompletableFuture.supplyAsync(() -> {
                    try {

                        StreamRecord record = queryRunner.query(String.format("select * from `%s` where AggregateRootId=? and CommandId=?", getTableName(aggregateRootId)),
                                new BeanHandler<>(StreamRecord.class),
                                aggregateRootId,
                                commandId);

                        DomainEventStream stream = record != null ? convertFrom(record) : null;
                        return new AsyncTaskResult<>(AsyncTaskStatus.Success, stream);
                    } catch (SQLException ex) {
                        logger.error(String.format("Find event by commandId has sql exception, aggregateRootId: %s, commandId: %s", aggregateRootId, commandId), ex);
                        return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
                    } catch (Exception ex) {
                        logger.error(String.format("Find event by commandId has unknown exception, aggregateRootId: %s, commandId: %s", aggregateRootId, commandId), ex);
                        return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
                    }
                }), "FindEventByCommandIdAsync");
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
                record.getCommandId(),
                record.getAggregateRootId(),
                record.getAggregateRootTypeName(),
                record.getVersion(),
                record.getCreatedOn(),
                eventSerializer.deserialize(JsonTool.deserialize(record.getEvents(), Map.class), IDomainEvent.class),
                null);
    }

    private StreamRecord convertTo(DomainEventStream eventStream) {
        return new StreamRecord(eventStream.getCommandId(), eventStream.getAggregateRootId(), eventStream.getAggregateRootTypeName(),
                eventStream.getVersion(), eventStream.getTimestamp(),
                JsonTool.serialize(eventSerializer.serialize(eventStream.events())));
    }
}

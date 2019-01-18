package com.enode.infrastructure.impl.mysql;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.utilities.Ensure;
import com.enode.configurations.OptionSetting;
import com.enode.infrastructure.IMessageHandleRecordStore;
import com.enode.infrastructure.MessageHandleRecord;
import com.enode.infrastructure.ThreeMessageHandleRecord;
import com.enode.infrastructure.TwoMessageHandleRecord;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class MysqlMessageHandleRecordStore implements IMessageHandleRecordStore {

    private static final Logger _logger = ENodeLogger.getLog();

    private final QueryRunner _queryRunner;
    private final String _oneMessageTableName;
    private final String _oneMessageTableUniqueIndexName;
    private final String _twoMessageTableName;
    private final String _twoMessageTableUniqueIndexName;
    private final String _threeMessageTableName;
    private final String _threeMessageTableUniqueIndexName;

    public MysqlMessageHandleRecordStore(DataSource ds, OptionSetting optionSetting) {
        Ensure.notNull(ds, "ds");
        Ensure.notNull(optionSetting, "optionSetting");

        _queryRunner = new QueryRunner(ds);
        _oneMessageTableName = optionSetting.getOptionValue("OneMessageTableName");
        _oneMessageTableUniqueIndexName = optionSetting.getOptionValue("OneMessageTableUniqueIndexName");
        _twoMessageTableName = optionSetting.getOptionValue("TwoMessageTableName");
        _twoMessageTableUniqueIndexName = optionSetting.getOptionValue("TwoMessageTableUniqueIndexName");
        _threeMessageTableName = optionSetting.getOptionValue("ThreeMessageTableName");
        _threeMessageTableUniqueIndexName = optionSetting.getOptionValue("ThreeMessageTableUniqueIndexName");

        Ensure.notNull(_oneMessageTableName, "_oneMessageTableName");
        Ensure.notNull(_oneMessageTableUniqueIndexName, "_oneMessageTableUniqueIndexName");
        Ensure.notNull(_twoMessageTableName, "_twoMessageTableName");
        Ensure.notNull(_twoMessageTableUniqueIndexName, "_twoMessageTableUniqueIndexName");
        Ensure.notNull(_threeMessageTableName, "_threeMessageTableName");
        Ensure.notNull(_threeMessageTableUniqueIndexName, "_threeMessageTableUniqueIndexName");
    }

    @Override
    public CompletableFuture<AsyncTaskResult> addRecordAsync(MessageHandleRecord record) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                _queryRunner.update(String.format("INSERT INTO %s(MessageId,HandlerTypeName,MessageTypeName,AggregateRootTypeName,AggregateRootId,Version,CreatedOn) VALUES(?,?,?,?,?,?,?)", _oneMessageTableName),
                        record.getMessageId(),
                        record.getHandlerTypeName(),
                        record.getMessageTypeName(),
                        record.getAggregateRootTypeName(),
                        record.getAggregateRootId(),
                        record.getVersion(),
                        record.getCreatedOn());

                return AsyncTaskResult.Success;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(_oneMessageTableUniqueIndexName)) {
                    return AsyncTaskResult.Success;
                }
                _logger.error("Insert message handle record has sql exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                _logger.error("Insert message handle record has unknown exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult> addRecordAsync(TwoMessageHandleRecord record) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                _queryRunner.update(String.format("INSERT INTO %s(MessageId1,MessageId2,HandlerTypeName,Message1TypeName,Message2TypeName,AggregateRootTypeName,AggregateRootId,Version,CreatedOn) VALUES(?,?,?,?,?,?,?,?,?)", _twoMessageTableName),
                        record.getMessageId1(),
                        record.getMessageId2(),
                        record.getHandlerTypeName(),
                        record.getMessage1TypeName(),
                        record.getMessage2TypeName(),
                        record.getAggregateRootTypeName(),
                        record.getAggregateRootId(),
                        record.getVersion(),
                        record.getCreatedOn());

                return AsyncTaskResult.Success;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(_twoMessageTableUniqueIndexName)) {
                    return AsyncTaskResult.Success;
                }
                _logger.error("Insert two-message handle record has sql exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                _logger.error("Insert two-message handle record has unknown exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult> addRecordAsync(ThreeMessageHandleRecord record) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                _queryRunner.update(String.format("INSERT INTO %s(MessageId1,MessageId2,MessageId3,HandlerTypeName,Message1TypeName,Message2TypeName,Message3TypeName,AggregateRootTypeName,AggregateRootId,Version,CreatedOn) VALUES(?,?,?,?,?,?,?,?,?,?,?)", _threeMessageTableName),
                        record.getMessageId1(),
                        record.getMessageId2(),
                        record.getMessageId3(),
                        record.getHandlerTypeName(),
                        record.getMessage1TypeName(),
                        record.getMessage2TypeName(),
                        record.getMessage3TypeName(),
                        record.getAggregateRootTypeName(),
                        record.getAggregateRootId(),
                        record.getVersion(),
                        record.getCreatedOn());

                return AsyncTaskResult.Success;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(_threeMessageTableUniqueIndexName)) {
                    return AsyncTaskResult.Success;
                }
                _logger.error("Insert three-message handle record has sql exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                _logger.error("Insert three-message handle record has unknown exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId, String handlerTypeName, String aggregateRootTypeName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object countObj = (int) (long) _queryRunner.query(String.format("SELECT COUNT(*) FROM %s WHERE MessageId=? AND HandlerTypeName=?", _oneMessageTableName),
                        new ScalarHandler<>(),
                        messageId, handlerTypeName);

                int count = (countObj == null ? 0 : ((Number) countObj).intValue());

                return new AsyncTaskResult<>(AsyncTaskStatus.Success, count > 0);
            } catch (SQLException ex) {
                _logger.error("Get message handle record has sql exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                _logger.error("Get message handle record has unknown exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId1, String messageId2, String handlerTypeName, String aggregateRootTypeName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object countObj = _queryRunner.query(String.format("SELECT COUNT(*) FROM %s WHERE MessageId1=? AND MessageId2=? and HandlerTypeName=?", _twoMessageTableName),
                        new ScalarHandler<>(),
                        messageId1,
                        messageId2,
                        handlerTypeName);

                int count = (countObj == null ? 0 : ((Number) countObj).intValue());

                return new AsyncTaskResult<>(AsyncTaskStatus.Success, count > 0);
            } catch (SQLException ex) {
                _logger.error("Get two-message handle record has sql exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                _logger.error("Get two-message handle record has unknown exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId1, String messageId2, String messageId3, String handlerTypeName, String aggregateRootTypeName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object countObj = (int) (long) _queryRunner.query(String.format("SELECT COUNT(*) FROM %s WHERE MessageId1=? AND MessageId2=? AND MessageId3=? AND HandlerTypeName=?", _threeMessageTableName),
                        new ScalarHandler<>(),
                        messageId1,
                        messageId2,
                        messageId3,
                        handlerTypeName);

                int count = (countObj == null ? 0 : ((Number) countObj).intValue());

                return new AsyncTaskResult<>(AsyncTaskStatus.Success, count > 0);
            } catch (SQLException ex) {
                _logger.error("Get three-message handle record has sql exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                _logger.error("Get three-message handle record has unknown exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }
}

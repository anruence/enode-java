package com.enodeframework.mysql;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.configurations.DefaultDBConfigurationSetting;
import com.enodeframework.configurations.OptionSetting;
import com.enodeframework.infrastructure.IPublishedVersionStore;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author anruence@gmail.com
 */
public class MysqlPublishedVersionStore implements IPublishedVersionStore {
    private static final Logger logger = LoggerFactory.getLogger(MysqlPublishedVersionStore.class);
    private final QueryRunner queryRunner;
    private final String tableName;
    private final String uniqueIndexName;
    private final Executor executor;

    public MysqlPublishedVersionStore(DataSource ds, OptionSetting optionSetting) {
        Ensure.notNull(ds, "ds");
        if (optionSetting != null) {
            tableName = optionSetting.getOptionValue("TableName");
            uniqueIndexName = optionSetting.getOptionValue("UniqueIndexName");
        } else {
            DefaultDBConfigurationSetting setting = new DefaultDBConfigurationSetting();
            tableName = setting.getPublishedVersionTableName();
            uniqueIndexName = setting.getPublishedVersionUniqueIndexName();
        }
        Ensure.notNull(tableName, "tableName");
        Ensure.notNull(uniqueIndexName, "uniqueIndexName");
        queryRunner = new QueryRunner(ds);
        executor = new ThreadPoolExecutor(4, 4,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("MysqlPublishedVersionStoreExecutor-%d").build());
    }

    @Override
    public CompletableFuture<AsyncTaskResult> updatePublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId, int publishedVersion) {
        if (publishedVersion == 1) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    queryRunner.update(String.format("INSERT INTO %s(ProcessorName,AggregateRootTypeName,AggregateRootId,Version,CreatedOn) VALUES(?,?,?,?,?)", tableName),
                            processorName,
                            aggregateRootTypeName,
                            aggregateRootId,
                            1,
                            new Timestamp(System.currentTimeMillis()));
                    return AsyncTaskResult.Success;
                } catch (SQLException ex) {
                    if (ex.getErrorCode() == 1062 && ex.getMessage().contains(uniqueIndexName)) {
                        return AsyncTaskResult.Success;
                    }
                    logger.error("Insert aggregate published version has sql exception.", ex);
                    return new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage());
                } catch (Exception ex) {
                    logger.error("Insert aggregate published version has unknown exception.", ex);
                    return new AsyncTaskResult(AsyncTaskStatus.Failed, ex.getMessage());
                }
            }, executor);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    queryRunner.update(String.format("UPDATE %s set Version=?,CreatedOn=? WHERE ProcessorName=? and AggregateRootId=? and Version=?", tableName),
                            publishedVersion,
                            new Timestamp(System.currentTimeMillis()),
                            processorName,
                            aggregateRootId,
                            publishedVersion - 1);
                    return AsyncTaskResult.Success;
                } catch (SQLException ex) {
                    logger.error("Update aggregate published version has sql exception.", ex);
                    return new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage());
                } catch (Exception ex) {
                    logger.error("Update aggregate published version has unknown exception.", ex);
                    return new AsyncTaskResult(AsyncTaskStatus.Failed, ex.getMessage());
                }
            }, executor);
        }
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Integer>> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object resultObj = queryRunner.query(String.format("SELECT Version FROM %s WHERE ProcessorName=? AND AggregateRootId=?", tableName),
                        new ScalarHandler<>(), processorName, aggregateRootId);
                int result = (resultObj == null ? 0 : ((Number) resultObj).intValue());
                return new AsyncTaskResult<>(AsyncTaskStatus.Success, result);
            } catch (SQLException ex) {
                logger.error("Get aggregate published version has sql exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                logger.error("Get aggregate published version has unknown exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
            }
        }, executor);
    }
}

package com.enodeframework.mysql;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.configurations.DefaultDBConfigurationSetting;
import com.enodeframework.configurations.OptionSetting;
import com.enodeframework.infrastructure.IPublishedVersionStore;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class MysqlPublishedVersionStoreVertx implements IPublishedVersionStore {
    private static final Logger logger = LoggerFactory.getLogger(MysqlPublishedVersionStoreVertx.class);
    private final SQLClient sqlClient;
    private final String tableName;
    private final String uniqueIndexName;

    public MysqlPublishedVersionStoreVertx(DataSource ds, OptionSetting optionSetting) {
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
        Vertx vertx = Vertx.vertx();
        sqlClient = JDBCClient.create(vertx, ds);
    }

    @Override
    public CompletableFuture<AsyncTaskResult> updatePublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId, int publishedVersion) {
        CompletableFuture<AsyncTaskResult> future = new CompletableFuture<>();
        String sql = "";
        JsonArray array = new JsonArray();
        if (publishedVersion == 1) {
            sql = String.format("INSERT INTO %s(ProcessorName,AggregateRootTypeName,AggregateRootId,Version,CreatedOn) VALUES(?,?,?,?,?)", tableName);
            array.add(processorName);
            array.add(aggregateRootTypeName);
            array.add(aggregateRootId);
            array.add(1);
            array.add(new Date().toInstant());
        } else {
            sql = String.format("UPDATE %s set Version=?,CreatedOn=? WHERE ProcessorName=? and AggregateRootId=? and Version=?", tableName);
            array.add(publishedVersion);
            array.add(new Date().toInstant());
            array.add(processorName);
            array.add(aggregateRootId);
            array.add(publishedVersion - 1);
        }
        sqlClient.updateWithParams(sql, array, x -> {
            if (x.succeeded()) {
                future.complete(AsyncTaskResult.Success);
                return;
            }
            if (x.cause() instanceof SQLException) {
                SQLException ex = (SQLException) x.cause();
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(uniqueIndexName)) {
                    future.complete(AsyncTaskResult.Success);
                }
                logger.error("Insert or update aggregate published version has sql exception.", ex);
                future.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage()));
                return;
            }
            logger.error("Insert or update aggregate published version has unknown exception.", x.cause());
            future.complete(new AsyncTaskResult(AsyncTaskStatus.Failed, x.cause().getMessage()));
        });
        return future;
    }


    @Override
    public CompletableFuture<AsyncTaskResult<Integer>> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        CompletableFuture<AsyncTaskResult<Integer>> future = new CompletableFuture<>();

        String sql = String.format("SELECT Version FROM %s WHERE ProcessorName=? AND AggregateRootId=?", tableName);
        JsonArray array = new JsonArray();
        array.add(processorName);
        array.add(aggregateRootId);
        sqlClient.queryWithParams(sql, array, x -> {
            if (x.succeeded()) {
                int result = 0;
                if (x.result().getNumRows() > 1) {
                    result = x.result().getRows().get(0).getInteger("Version");
                }
                future.complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, result));
                return;
            }
            Throwable ex = x.cause();
            if (x.cause() instanceof SQLException) {
                logger.error("Get aggregate published version has sql exception.", ex);
                future.complete(new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage()));
                return;
            }
            logger.error("Get aggregate published version has unknown exception.", ex);
            future.complete(new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage()));
        });
        return future;
    }
}

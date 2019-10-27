package org.enodeframework.mysql;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import org.enodeframework.ObjectContainer;
import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.configurations.DataSourceKey;
import org.enodeframework.configurations.DefaultDBConfigurationSetting;
import org.enodeframework.configurations.OptionSetting;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class MysqlPublishedVersionStore implements IPublishedVersionStore {
    private static final Logger logger = LoggerFactory.getLogger(MysqlPublishedVersionStore.class);
    private final SQLClient sqlClient;
    private final String tableName;
    private final String uniqueIndexName;

    public MysqlPublishedVersionStore(DataSource ds, OptionSetting optionSetting) {
        Ensure.notNull(ds, "ds");
        if (optionSetting != null) {
            tableName = optionSetting.getOptionValue(DataSourceKey.PUBLISHED_VERSION_TABLENAME);
            uniqueIndexName = optionSetting.getOptionValue(DataSourceKey.PUBLISHED_VERSION_UNIQUE_INDEX_NAME);
        } else {
            DefaultDBConfigurationSetting setting = new DefaultDBConfigurationSetting();
            tableName = setting.getPublishedVersionTableName();
            uniqueIndexName = setting.getPublishedVersionUniqueIndexName();
        }
        Ensure.notNull(tableName, "tableName");
        Ensure.notNull(uniqueIndexName, "uniqueIndexName");
        sqlClient = JDBCClient.create(ObjectContainer.vertx, ds);
    }

    @Override
    public CompletableFuture<Void> updatePublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId, int publishedVersion) {
        CompletableFuture<Void> future = new CompletableFuture<>();
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
                future.complete(null);
                return;
            }
            future.completeExceptionally(x.cause());
        });
        return future.exceptionally(throwable -> {
            if (throwable instanceof SQLException) {
                SQLException ex = (SQLException) throwable;
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(uniqueIndexName)) {
                    future.complete(null);
                }
                logger.error("Insert or update aggregate published version has sql exception.", ex);
                throw new IORuntimeException(throwable);
            }
            logger.error("Insert or update aggregate published version has unknown exception.", throwable);
            throw new ENodeRuntimeException(throwable);

        });
    }

    @Override
    public CompletableFuture<Integer> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String sql = String.format("SELECT Version FROM %s WHERE ProcessorName=? AND AggregateRootId=?", tableName);
        JsonArray array = new JsonArray();
        array.add(processorName);
        array.add(aggregateRootId);
        sqlClient.queryWithParams(sql, array, x -> {
            if (x.succeeded()) {
                int result = 0;
                Optional<JsonObject> first = x.result().getRows().stream().findFirst();
                if (first.isPresent()) {
                    result = first.get().getInteger("Version");
                }
                future.complete(result);
                return;
            }
            future.completeExceptionally(x.cause());
        });
        return future.exceptionally(throwable -> {
            if (throwable instanceof SQLException) {
                SQLException ex = (SQLException) throwable;
                logger.error("Get aggregate published version has sql exception.", ex);
                throw new IORuntimeException(throwable);
            }
            logger.error("Get aggregate published version has unknown exception.", throwable);
            throw new ENodeRuntimeException(throwable);
        });
    }
}

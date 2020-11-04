package org.enodeframework.jdbc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import org.enodeframework.common.exception.EventStoreException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class JDBCPublishedVersionStore extends AbstractVerticle implements IPublishedVersionStore {
    private static final Logger logger = LoggerFactory.getLogger(JDBCPublishedVersionStore.class);
    private static final String INSERT_SQL = "INSERT INTO %s (processor_name, aggregate_root_type_name, aggregate_root_id, version, gmt_create) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE %s SET version = ?, gmt_create = ? WHERE processor_name = ? AND aggregate_root_id = ? AND version = ?";
    private static final String SELECT_SQL = "SELECT version FROM %s WHERE processor_name = ? AND aggregate_root_id = ?";
    private static final String SELECT_MAX_VERSION_SQL = "SELECT max(version) AS version FROM %s WHERE aggregate_root_id = ? AND aggregate_root_type_name = ?";
    private final String tableName;
    private final String uniqueIndexName;
    private final String sqlState;
    private final DataSource dataSource;
    private SQLClient sqlClient;

    public JDBCPublishedVersionStore(DataSource dataSource) {
        this(dataSource, new DBConfiguration());
    }

    public JDBCPublishedVersionStore(DataSource dataSource, DBConfiguration setting) {
        Ensure.notNull(dataSource, "DataSource");
        Ensure.notNull(dataSource, "DBConfigurationSetting");
        this.dataSource = dataSource;
        this.tableName = setting.getPublishedVersionTableName();
        this.sqlState = setting.getSqlState();
        this.uniqueIndexName = setting.getPublishedVersionUniqueIndexName();
    }

    @Override
    public void start() {
        sqlClient = JDBCClient.create(vertx, dataSource);
    }

    @Override
    public CompletableFuture<Integer> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String sql = String.format(SELECT_MAX_VERSION_SQL, tableName);
        JsonArray array = new JsonArray();
        array.add(aggregateRootId);
        array.add(aggregateRootTypeName);
        sqlClient.querySingleWithParams(sql, array, x -> {
            if (x.succeeded()) {
                int result = 0;
                if (x.result() != null && x.result().size() > 0) {
                    result = Optional.ofNullable(x.result().getInteger(0)).orElse(0);
                }
                future.complete(result);
                return;
            }
            future.completeExceptionally(x.cause());
        });
        return future.exceptionally(throwable -> {
            if (throwable instanceof SQLException) {
                SQLException ex = (SQLException) throwable;
                logger.error("Find version by aggregateRootId has sql exception, aggregateRootId: {}, aggregateRootTypeName: {}", aggregateRootId, aggregateRootTypeName, ex);
                throw new IORuntimeException(throwable);
            }
            logger.error("Find version by aggregateRootId has unknown exception, aggregateRootId: {}, aggregateRootTypeName: {}", aggregateRootId, aggregateRootTypeName, throwable);
            throw new EventStoreException(throwable);
        });
    }
}

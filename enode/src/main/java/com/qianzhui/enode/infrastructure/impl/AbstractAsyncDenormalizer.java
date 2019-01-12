package com.qianzhui.enode.infrastructure.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.common.io.IORuntimeException;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

public abstract class AbstractAsyncDenormalizer {
    protected final DataSource ds;
    protected final QueryRunner _queryRunner;
    protected final Executor executor;

    public AbstractAsyncDenormalizer(DataSource ds) {
        this.ds = ds;
        this._queryRunner = new QueryRunner(ds);
        executor = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("AsyncDenormalizerExecutor-%d").build());
    }

    public CompletableFuture<AsyncTaskResult> tryExecuteAsync(Function<QueryRunner, AsyncTaskResult> executer) {
        return CompletableFuture.supplyAsync(() ->
                executer.apply(_queryRunner),
                executor
        );
    }

    public CompletableFuture<AsyncTaskResult> tryInsertRecordAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this._queryRunner.update(sql, params);
                return AsyncTaskResult.Success;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062) { //主键冲突，忽略即可；出现这种情况，是因为同一个消息的重复处理
                    return AsyncTaskResult.Success;
                }
                throw new IORuntimeException(ex.getMessage(), ex);
            }
        }, executor);
    }

    public CompletableFuture<AsyncTaskResult> tryInsertRecordAsync(InsertExecuter insertExecuter) {
        return tryInsertRecordAsync(insertExecuter.getSql(), insertExecuter.getParams());
    }

    public CompletableFuture<AsyncTaskResult> tryUpdateRecordAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this._queryRunner.update(sql, params);
                return AsyncTaskResult.Success;
            } catch (SQLException ex) {
                throw new IORuntimeException(ex.getMessage(), ex);
            }
        }, executor);
    }

    public CompletableFuture<AsyncTaskResult> tryUpdateRecordAsync(UpdateExecuter updateExecuter) {
        return tryUpdateRecordAsync(updateExecuter.getSql(), updateExecuter.getParams());
    }

    public CompletableFuture<AsyncTaskResult> tryTransactionAsync(QueryRunnerExecuter... executers) {
        return CompletableFuture.supplyAsync(() -> {

            try (Connection connection = ds.getConnection()) {
                boolean autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);

                try {
                    for (QueryRunnerExecuter executer : executers) {
                        executer.update(_queryRunner, connection);
                    }

                    connection.commit();

                    return AsyncTaskResult.Success;
                } catch (SQLException ex) {
                    connection.rollback();
                    throw new IORuntimeException(ex.getMessage(), ex);
                } finally {
                    if(autoCommit) {
                        connection.setAutoCommit(true);
                    }
                }
            } catch (SQLException ex) {
                throw new IORuntimeException(ex.getMessage(), ex);
            }
        }, executor);
    }

    protected InsertExecuter insertStatement(String sql, Object... params) {
        return new InsertExecuter(sql, params);
    }

    protected UpdateExecuter updateStatement(String sql, Object... params) {
        return new UpdateExecuter(sql, params);
    }

    protected QueryRunnerExecuter batchStatement(String sql, Object[][] params) {
        return new BatchExecuter(sql, params, true);
    }

    protected interface QueryRunnerExecuter {
        int update(QueryRunner queryRunner, Connection connection) throws SQLException;
    }

    static abstract class AbstractQueryRunnerExecuter implements QueryRunnerExecuter {
        private String sql;
        private Object[] params;

        public AbstractQueryRunnerExecuter(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }

        @Override
        public int update(QueryRunner queryRunner, Connection connection) throws SQLException {
            return queryRunner.update(connection, sql, params);
        }
    }

    public static class UpdateExecuter extends AbstractQueryRunnerExecuter {

        public UpdateExecuter(String sql, Object[] params) {
            super(sql, params);
        }
    }

    public static class InsertExecuter extends AbstractQueryRunnerExecuter {
        public InsertExecuter(String sql, Object[] params) {
            super(sql, params);
        }

        @Override
        public int update(QueryRunner queryRunner, Connection connection) throws SQLException {
            try {
                return queryRunner.update(connection, getSql(), getParams());
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062) { //主键冲突，忽略即可；出现这种情况，是因为同一个消息的重复处理
                    //ignore
                    return 1;
                }
                throw ex;
            }
        }
    }

    static class BatchExecuter implements QueryRunnerExecuter {

        private String sql;
        private Object[][] params;
        private boolean ignoreDuplicate;

        public BatchExecuter(String sql, Object[][] params, boolean ignoreDuplicate) {
            this.sql = sql;
            this.params = params;
            this.ignoreDuplicate = ignoreDuplicate;
        }

        @Override
        public int update(QueryRunner queryRunner, Connection connection) throws SQLException {
            try {
                queryRunner.batch(connection, sql, params);
                return 0;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062 && ignoreDuplicate) { //主键冲突，忽略即可；出现这种情况，是因为同一个消息的重复处理
                    insertOneByOne(queryRunner, connection);
                    return 0;
                }
                throw ex;
            }
        }

        private void insertOneByOne(QueryRunner queryRunner, Connection connection) throws SQLException {
            for (int i = 0; i < params.length; i++) {
                Object[] parameters = params[i];

                try {
                    queryRunner.update(connection, sql, parameters);
                } catch (SQLException ex) {
                    if (ex.getErrorCode() == 1062 && ignoreDuplicate) {
                        //ignore
                        return;
                    }

                    throw ex;
                }
            }
        }
    }
}

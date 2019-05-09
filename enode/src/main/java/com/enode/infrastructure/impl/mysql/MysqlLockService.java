package com.enode.infrastructure.impl.mysql;

import com.enode.common.function.Action;
import com.enode.common.utilities.Ensure;
import com.enode.configurations.DefaultDBConfigurationSetting;
import com.enode.configurations.OptionSetting;
import com.enode.infrastructure.ILockService;
import com.enode.infrastructure.WrappedRuntimeException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MysqlLockService implements ILockService {
    private final String _tableName;
    private final String _lockKeySqlFormat;
    private final DataSource _ds;
    private final QueryRunner _queryRunner;

    public MysqlLockService(DataSource ds, OptionSetting optionSetting) {
        Ensure.notNull(ds, "ds");

        if (optionSetting != null) {
            _tableName = optionSetting.getOptionValue("TableName");
        } else {
            DefaultDBConfigurationSetting setting = new DefaultDBConfigurationSetting();
            _tableName = setting.getLockKeyTableName();
        }

        Ensure.notNull(_tableName, "_tableName");

        _lockKeySqlFormat = "SELECT * FROM `" + _tableName + "` WHERE `Name` = ? FOR UPDATE";

        _ds = ds;
        _queryRunner = new QueryRunner(ds);
    }

    @Override
    public void addLockKey(String lockKey) {
        try {
            int count = (int) (long) _queryRunner.query(String.format("SELECT COUNT(*) FROM %s WHERE NAME=?", _tableName), new ScalarHandler<>(), lockKey);
            if (count == 0) {
                _queryRunner.update(String.format("INSERT INTO %s VALUES(?)", _tableName), lockKey);
            }
        } catch (SQLException ex) {
            throw new WrappedRuntimeException(ex);
        }
    }

    @Override
    public void executeInLock(String lockKey, Action action) {

        try (Connection connection = getConnection()) {
            try {
                connection.setAutoCommit(false);
                lockKey(connection, lockKey);
                action.apply();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
            } catch (Exception e) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            throw new WrappedRuntimeException(ex);
        }
    }

    private void lockKey(Connection connection, String key) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(_lockKeySqlFormat);
        statement.setString(1, key);
        statement.executeQuery();
    }

    private Connection getConnection() throws SQLException {
        return _ds.getConnection();
    }
}

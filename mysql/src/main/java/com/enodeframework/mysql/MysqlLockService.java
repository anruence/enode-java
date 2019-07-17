package com.enodeframework.mysql;

import com.enodeframework.common.exception.ENodeRuntimeException;
import com.enodeframework.common.function.Action;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.configurations.DefaultDBConfigurationSetting;
import com.enodeframework.configurations.OptionSetting;
import com.enodeframework.infrastructure.ILockService;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author anruence@gmail.com
 */
public class MysqlLockService implements ILockService {
    private final String tableName;
    private final String lockKeySqlFormat;
    private final DataSource ds;
    private final QueryRunner queryRunner;

    public MysqlLockService(DataSource ds, OptionSetting optionSetting) {
        Ensure.notNull(ds, "ds");

        if (optionSetting != null) {
            tableName = optionSetting.getOptionValue("TableName");
        } else {
            DefaultDBConfigurationSetting setting = new DefaultDBConfigurationSetting();
            tableName = setting.getLockKeyTableName();
        }

        Ensure.notNull(tableName, "tableName");

        lockKeySqlFormat = "SELECT * FROM `" + tableName + "` WHERE `Name` = ? FOR UPDATE";

        this.ds = ds;
        queryRunner = new QueryRunner(ds);
    }

    @Override
    public void addLockKey(String lockKey) {
        try {
            int count = (int) (long) queryRunner.query(String.format("SELECT COUNT(*) FROM %s WHERE NAME=?", tableName), new ScalarHandler<>(), lockKey);
            if (count == 0) {
                queryRunner.update(String.format("INSERT INTO %s VALUES(?)", tableName), lockKey);
            }
        } catch (SQLException ex) {
            throw new ENodeRuntimeException(ex);
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
            } catch (Exception e) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            throw new ENodeRuntimeException(ex);
        }
    }

    private void lockKey(Connection connection, String key) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(lockKeySqlFormat);
        statement.setString(1, key);
        statement.executeQuery();
    }

    private Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}

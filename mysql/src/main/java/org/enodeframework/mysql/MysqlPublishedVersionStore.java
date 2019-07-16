package org.enodeframework.mysql;

import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.jdbc.JDBCPublishedVersionStore;

import javax.sql.DataSource;

/**
 * @author anruence@gmail.com
 */
public class MysqlPublishedVersionStore extends JDBCPublishedVersionStore {

    public MysqlPublishedVersionStore(DataSource dataSource) {
        super(dataSource);
    }

    public MysqlPublishedVersionStore(DataSource dataSource, DBConfiguration setting) {
        super(dataSource, setting);
    }
}
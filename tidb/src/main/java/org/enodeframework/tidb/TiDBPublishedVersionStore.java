package org.enodeframework.tidb;

import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.jdbc.JDBCPublishedVersionStore;

import javax.sql.DataSource;

/**
 * @author anruence@gmail.com
 */
public class TiDBPublishedVersionStore extends JDBCPublishedVersionStore {

    public TiDBPublishedVersionStore(DataSource dataSource) {
        super(dataSource);
    }

    public TiDBPublishedVersionStore(DataSource dataSource, DBConfiguration setting) {
        super(dataSource, setting);
    }
}
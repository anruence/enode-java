package org.enodeframework.pg;

import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.jdbc.JDBCPublishedVersionStore;

import javax.sql.DataSource;

/**
 * @author anruence@gmail.com
 */
public class PgPublishedVersionStore extends JDBCPublishedVersionStore {

    public PgPublishedVersionStore(DataSource dataSource) {
        super(dataSource);
    }

    public PgPublishedVersionStore(DataSource dataSource, DBConfiguration setting) {
        super(dataSource, setting);
    }
}
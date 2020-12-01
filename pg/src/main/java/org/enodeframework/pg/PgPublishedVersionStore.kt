package org.enodeframework.pg

import org.enodeframework.jdbc.DBConfiguration
import org.enodeframework.jdbc.JDBCPublishedVersionStore
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
class PgPublishedVersionStore : JDBCPublishedVersionStore {
    constructor(dataSource: DataSource) : super(dataSource) {}
    constructor(dataSource: DataSource, setting: DBConfiguration) : super(dataSource, setting) {}
}
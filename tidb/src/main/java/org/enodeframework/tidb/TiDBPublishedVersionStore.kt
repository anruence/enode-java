package org.enodeframework.tidb

import org.enodeframework.jdbc.DBConfiguration
import org.enodeframework.jdbc.JDBCPublishedVersionStore
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
class TiDBPublishedVersionStore : JDBCPublishedVersionStore {
    constructor(dataSource: DataSource) : super(dataSource) {}
    constructor(dataSource: DataSource, setting: DBConfiguration) : super(dataSource, setting) {}
}
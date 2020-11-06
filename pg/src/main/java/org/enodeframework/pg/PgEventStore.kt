package org.enodeframework.pg

import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.eventing.IEventSerializer
import org.enodeframework.jdbc.DBConfiguration
import org.enodeframework.jdbc.JDBCEventStore
import java.util.regex.Pattern
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
class PgEventStore : JDBCEventStore {
    constructor(dataSource: DataSource, eventSerializer: IEventSerializer?, serializeService: ISerializeService?) : super(dataSource, eventSerializer!!, serializeService!!) {}
    constructor(dataSource: DataSource, setting: DBConfiguration?, eventSerializer: IEventSerializer?, serializeService: ISerializeService?) : super(dataSource, setting!!, eventSerializer!!, serializeService!!) {}

    public override fun parseDuplicateCommandId(errMsg: String?): String {
        val matcher = PATTERN_POSTGRESQL.matcher(errMsg)
        if (!matcher.find()) {
            return ""
        }
        return if (matcher.groupCount() == 0) {
            ""
        } else matcher.group(1)
    }

    companion object {
        private val PATTERN_POSTGRESQL = Pattern.compile("=\\(.*, (.*)\\) already exists.$")
    }
}
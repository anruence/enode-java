package org.enodeframework.mysql

import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.eventing.IEventSerializer
import org.enodeframework.jdbc.DBConfiguration
import org.enodeframework.jdbc.JDBCEventStore
import java.util.regex.Pattern
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
class MysqlEventStore(dataSource: DataSource, setting: DBConfiguration, eventSerializer: IEventSerializer, serializeService: ISerializeService) : JDBCEventStore(dataSource, setting, eventSerializer, serializeService) {

    /**
     * Duplicate entry '5d3ac841d1fcfe669e9a257d-5d3ac841d1fcfe669e9a2585' for key 'IX_EventStream_AggId_CommandId'
     */
    override fun getDuplicatedId(throwable: Throwable): String {
        val matcher = PATTERN_MYSQL.matcher(throwable.message!!)
        if (!matcher.find()) {
            return ""
        }
        return if (matcher.groupCount() == 0) {
            ""
        } else matcher.group(1)
    }

    companion object {
        private val PATTERN_MYSQL = Pattern.compile("^Duplicate entry '.*-(.*)' for key")
    }
}
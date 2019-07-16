package org.enodeframework.tidb;

import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.jdbc.JDBCEventStore;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author anruence@gmail.com
 */
public class TiDBEventStore extends JDBCEventStore {

    private static final Pattern PATTERN_MYSQL = Pattern.compile("^Duplicate entry '.*-(.*)' for key");

    public TiDBEventStore(DataSource dataSource, IEventSerializer eventSerializer, ISerializeService serializeService) {
        super(dataSource, eventSerializer, serializeService);
    }

    public TiDBEventStore(DataSource dataSource, DBConfiguration setting, IEventSerializer eventSerializer, ISerializeService serializeService) {
        super(dataSource, setting, eventSerializer, serializeService);
    }

    @Override
    public String parseDuplicateCommandId(String errMsg) {
        Matcher matcher = PATTERN_MYSQL.matcher(errMsg);
        if (!matcher.find()) {
            return "";
        }
        if (matcher.groupCount() == 0) {
            return "";
        }
        return matcher.group(1);
    }
}

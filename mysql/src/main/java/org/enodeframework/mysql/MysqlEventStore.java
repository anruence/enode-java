package org.enodeframework.mysql;

import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.jdbc.JDBCEventStore;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author anruence@gmail.com
 */
public class MysqlEventStore extends JDBCEventStore {

    private static final Pattern PATTERN_MYSQL = Pattern.compile("^Duplicate entry '.*-(.*)' for key");

    public MysqlEventStore(DataSource dataSource, IEventSerializer eventSerializer) {
        super(dataSource, eventSerializer);
    }

    public MysqlEventStore(DataSource dataSource, DBConfiguration setting, IEventSerializer eventSerializer) {
        super(dataSource, setting, eventSerializer);
    }

    @Override
    public String findDuplicateCommandInException(String errMsg) {
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

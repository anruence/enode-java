package org.enodeframework.pg;

import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.jdbc.JDBCEventStore;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author anruence@gmail.com
 */
public class PgEventStore extends JDBCEventStore {

    private static final Pattern PATTERN_POSTGRESQL = Pattern.compile("=\\(.*, (.*)\\) already exists.$");

    public PgEventStore(DataSource dataSource) {
        super(dataSource);
    }

    public PgEventStore(DataSource dataSource, DBConfiguration setting) {
        super(dataSource, setting);
    }

    @Override
    public String findDuplicateCommandInException(String errMsg) {
        Matcher matcher = PATTERN_POSTGRESQL.matcher(errMsg);
        if (!matcher.find()) {
            return "";
        }
        if (matcher.groupCount() == 0) {
            return "";
        }
        return matcher.group(1);
    }
}

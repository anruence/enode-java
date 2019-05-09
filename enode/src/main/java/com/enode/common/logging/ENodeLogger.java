package com.enode.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ENodeLogger {

    public static Logger log;

    static {
        log = createLogger("ENodeLog");
    }

    private static Logger createLogger(final String loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        ENodeLogger.log = log;
    }
}

package org.enodeframework.common.scheduling;

import org.enodeframework.common.function.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author anruence@gmail.com
 */
public class Worker {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private final Object lockObject = new Object();
    private final String actionName;
    private final Action action;
    private Status status;
    private Thread thread;

    public Worker(String actionName, Action action) {
        this.actionName = actionName;
        this.action = action;
        this.status = Status.Initial;
    }

    public Worker start() {
        synchronized (lockObject) {
            if (status.equals(Status.Running)) {
                return this;
            }
            status = Status.Running;
            thread = new Thread(this::loop, String.format("%s.Worker", actionName));
            thread.setDaemon(true);
            thread.start();
            return this;
        }
    }

    public Worker stop() {
        synchronized (lockObject) {
            if (status.equals(Status.StopRequested)) {
                return this;
            }
            status = Status.StopRequested;
            thread.interrupt();
            logger.info("Worker thread shutdown, thread id:{}", thread.getName());
            return this;
        }
    }

    private void loop() {
        while (this.status == Status.Running) {
            try {
                action.apply();
            } catch (Exception ex) {
                logger.error("Worker thread has exception, actionName: {}", actionName, ex);
            }
        }
    }

    enum Status {
        Initial,
        Running,
        StopRequested
    }
}

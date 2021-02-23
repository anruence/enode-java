package org.enodeframework.common.scheduling;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.enodeframework.common.function.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author anruence@gmail.com
 */
public class DefaultScheduleService implements IScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultScheduleService.class);
    private final Object lockObject = new Object();
    private final Map<String, TimerBasedTask> taskDict = new HashMap<>();
    private final ScheduledExecutorService scheduledThreadPool;

    public DefaultScheduleService() {
        scheduledThreadPool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ScheduleService-%d").build());
    }

    @Override
    public void startTask(String name, Action action, int dueTime, int period) {
        synchronized (lockObject) {
            if (taskDict.containsKey(name)) {
                return;
            }
            ScheduledFuture<?> scheduledFuture = scheduledThreadPool.scheduleWithFixedDelay(new TaskCallback(name), dueTime, period, TimeUnit.MILLISECONDS);
            taskDict.put(name, new TimerBasedTask(name, action, scheduledFuture, dueTime, period, false));
        }
    }

    @Override
    public void stopTask(String name) {
        synchronized (lockObject) {
            if (taskDict.containsKey(name)) {
                TimerBasedTask task = taskDict.get(name);
                task.setStopped(true);
                task.getScheduledFuture().cancel(false);
                taskDict.remove(name);
            }
        }
    }

    static class TimerBasedTask {
        private String name;
        private Action action;
        private ScheduledFuture<?> scheduledFuture;
        private int dueTime;
        private int period;
        private boolean stopped;

        public TimerBasedTask(String name, Action action, ScheduledFuture<?> scheduledFuture, int dueTime, int period, boolean stopped) {
            this.name = name;
            this.action = action;
            this.scheduledFuture = scheduledFuture;
            this.dueTime = dueTime;
            this.period = period;
            this.stopped = stopped;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Action getAction() {
            return action;
        }

        public void setAction(Action action) {
            this.action = action;
        }

        public ScheduledFuture<?> getScheduledFuture() {
            return scheduledFuture;
        }

        public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        public int getDueTime() {
            return dueTime;
        }

        public void setDueTime(int dueTime) {
            this.dueTime = dueTime;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        public boolean isStopped() {
            return stopped;
        }

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }
    }

    class TaskCallback implements Runnable {
        private String taskName;

        public TaskCallback(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public void run() {
            TimerBasedTask task = taskDict.get(taskName);
            if (task != null) {
                try {
                    if (!task.isStopped()) {
                        task.action.apply();
                    }
                } catch (Exception ex) {
                    logger.error("Task has exception, name: {}, due: {}, period: {}", task.getName(), task.getDueTime(), task.getPeriod(), ex);
                }
            }
        }
    }
}

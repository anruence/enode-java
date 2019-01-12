package com.qianzhui.enode.common.scheduling;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.qianzhui.enode.common.function.Action;
import com.qianzhui.enode.common.logging.ENodeLogger;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduleService implements IScheduleService {
    private static final Logger _logger = ENodeLogger.getLog();

    private Object _lockObject = new Object();
    private Map<String, TimerBasedTask> _taskDict = new HashMap<>();
    private ScheduledExecutorService scheduledThreadPool;

    public ScheduleService() {
        scheduledThreadPool = Executors.newScheduledThreadPool(2,
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ScheduleService-%d").build());
    }

    @Override
    public void startTask(String name, Action action, int dueTime, int period) {
        synchronized (_lockObject) {
            if (_taskDict.containsKey(name)) {
                return;
            }

            ScheduledFuture<?> scheduledFuture = scheduledThreadPool.scheduleWithFixedDelay(new TaskCallback(name), dueTime, period, TimeUnit.MILLISECONDS);

            _taskDict.put(name, new TimerBasedTask(name, action, scheduledFuture, dueTime, period, false));
        }
    }

    @Override
    public void stopTask(String name) {
        synchronized (_lockObject) {
            if (_taskDict.containsKey(name)) {
                TimerBasedTask task = _taskDict.get(name);
                task.setStopped(true);
                task.getScheduledFuture().cancel(false);
                _taskDict.remove(name);
            }
        }
    }

    class TaskCallback implements Runnable {

        private String taskName;

        public TaskCallback(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public void run() {
            TimerBasedTask task = _taskDict.get(taskName);

            if (task != null) {
                try {
                    if (!task.isStopped()) {
//                        task.getScheduledFuture().cancel(false);
//                        task.Timer.Change(Timeout.Infinite, Timeout.Infinite);
                        task.action.apply();
                    }
                } catch (Exception ex) {
                    _logger.error(String.format("Task has exception, name: %s, due: %d, period: %d", task.getName(), task.getDueTime(), task.getPeriod()), ex);
                }
            }
        }
    }

    class TimerBasedTask {
        private String name;
        private Action action;
        private ScheduledFuture scheduledFuture;
        private int dueTime;
        private int period;
        private boolean stopped;

        public TimerBasedTask(String name, Action action, ScheduledFuture scheduledFuture, int dueTime, int period, boolean stopped) {
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

        public ScheduledFuture getScheduledFuture() {
            return scheduledFuture;
        }

        public void setScheduledFuture(ScheduledFuture scheduledFuture) {
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
}

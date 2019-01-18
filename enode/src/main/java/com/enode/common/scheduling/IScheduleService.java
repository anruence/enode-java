package com.enode.common.scheduling;

import com.enode.common.function.Action;

public interface IScheduleService {
    void startTask(String name, Action action, int dueTime, int period);

    void stopTask(String name);
}

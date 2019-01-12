package com.qianzhui.enode.common.scheduling;

import com.qianzhui.enode.common.function.Action;

public interface IScheduleService {
    void startTask(String name, Action action, int dueTime, int period);

    void stopTask(String name);
}

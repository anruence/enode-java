package com.qianzhui.enode.common.threading;

public class ManualResetEvent {
    private final Object monitor = new Object();
    private volatile boolean open = false;

    public ManualResetEvent(boolean initialState) {
        open = initialState;
    }

    public boolean waitOne() {
        synchronized (monitor) {
            if (!open) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return open;
        }
    }

    public boolean waitOne(long timeout) {
        synchronized (monitor) {
            if (!open) {
                try {
                    monitor.wait(timeout);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return open;
        }
    }

    public void set() {
        synchronized (monitor) {
            open = true;
            monitor.notifyAll();
        }
    }

    public void reset() {
        open = false;
    }
}


package org.enodeframework.common.extensions;

/**
 * A NoOp MessageMonitor callback
 */
public enum NoOpMessageMonitorCallback implements MessageMonitor.MonitorCallback {

    /**
     * Singleton instance of a {@link NoOpMessageMonitorCallback}.
     */
    INSTANCE;

    @Override
    public void reportSuccess() {
    }

    @Override
    public void reportFailure(Throwable cause) {
    }

    @Override
    public void reportIgnored() {

    }
}

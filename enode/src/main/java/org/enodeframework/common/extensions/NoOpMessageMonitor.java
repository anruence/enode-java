package org.enodeframework.common.extensions;

import org.enodeframework.messaging.IMessage;

/**
 * A message monitor that returns a NoOp message callback
 */
public enum NoOpMessageMonitor implements MessageMonitor<IMessage> {

    /**
     * Singleton instance of a {@link NoOpMessageMonitor}.
     */
    INSTANCE;

    /**
     * Returns the instance of {@code {@link NoOpMessageMonitor}}.
     * This method is a convenience method, which can be used as a lambda expression
     *
     * @return the instance of {@code {@link NoOpMessageMonitor}}
     */
    public static NoOpMessageMonitor instance() {
        return INSTANCE;
    }

    @Override
    public MonitorCallback onMessageIngested(IMessage message) {
        return NoOpMessageMonitorCallback.INSTANCE;
    }

}

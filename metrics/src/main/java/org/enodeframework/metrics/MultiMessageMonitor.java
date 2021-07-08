package org.enodeframework.metrics;

import org.enodeframework.common.extensions.MessageMonitor;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.messaging.IMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Delegates messages and callbacks to the given list of message monitors
 */
public class MultiMessageMonitor<T extends IMessage> implements MessageMonitor<T> {

    private final List<MessageMonitor<? super T>> messageMonitors;

    /**
     * Initialize a message monitor with the given <name>messageMonitors</name>
     *
     * @param messageMonitors the list of event monitors to delegate to
     */
    @SafeVarargs
    public MultiMessageMonitor(MessageMonitor<? super T>... messageMonitors) {
        this(Arrays.asList(messageMonitors));
    }

    /**
     * Initialize a message monitor with the given list of <name>messageMonitors</name>
     *
     * @param messageMonitors the list of event monitors to delegate to
     */
    public MultiMessageMonitor(List<MessageMonitor<? super T>> messageMonitors) {
        Assert.notNull(messageMonitors, () -> "MessageMonitor list may not be null");
        this.messageMonitors = new ArrayList<>(messageMonitors);
    }

    /**
     * Calls the message monitors with the given message and returns a callback
     * that will trigger all the message monitor callbacks
     *
     * @param message the message to delegate to the message monitors
     * @return the callback that will trigger all the message monitor callbacks
     */
    @Override
    public MonitorCallback onMessageIngested(T message) {
        final List<MonitorCallback> monitorCallbacks = messageMonitors.stream()
            .map(messageMonitor -> messageMonitor.onMessageIngested(message))
            .collect(Collectors.toList());

        return new MonitorCallback() {
            @Override
            public void reportSuccess() {
                monitorCallbacks.forEach(MonitorCallback::reportSuccess);
            }

            @Override
            public void reportFailure(Throwable cause) {
                monitorCallbacks.forEach(resultCallback -> resultCallback.reportFailure(cause));
            }

            @Override
            public void reportIgnored() {
                monitorCallbacks.forEach(MonitorCallback::reportIgnored);
            }
        };
    }
}

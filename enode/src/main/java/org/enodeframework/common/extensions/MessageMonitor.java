package org.enodeframework.common.extensions;

import org.enodeframework.messaging.IMessage;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Specifies a mechanism to monitor message processing. When a message is supplied to
 * a message monitor it returns a callback which should be used to notify the message monitor
 * of the result of the processing of the event.
 * <p>
 * For example, a message monitor can track various things like message processing times, failure and success rates and
 * occurred exceptions. It also can gather information contained in messages headers like timestamps and tracers
 */
public interface MessageMonitor<T extends IMessage> {
    /**
     * Takes a message and returns a callback that should be used
     * to inform the message monitor about the result of processing the message
     *
     * @param message the message to monitor
     * @return the callback
     */
    MonitorCallback onMessageIngested(T message);

    /**
     * Takes a collection of messages and returns a map containing events along with their callbacks
     *
     * @param messages to monitor
     * @return map where key = event and value = the callback
     */
    default Map<? super T, MonitorCallback> onMessagesIngested(Collection<? extends T> messages) {
        return messages.stream().collect(Collectors.toMap(msg -> msg, this::onMessageIngested));
    }

    /**
     * An interface to let the message processor inform the message monitor of the result
     * of processing the message
     */
    interface MonitorCallback {

        /**
         * Notify the monitor that the message was handled successfully
         */
        void reportSuccess();

        /**
         * Notify the monitor that a failure occurred during processing of the message
         *
         * @param cause or {@code null} if unknown
         */
        void reportFailure(Throwable cause);

        /**
         * Notify the monitor that the message was ignored
         */
        void reportIgnored();
    }
}


package org.enodeframework.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.enodeframework.common.extensions.MessageMonitor;
import org.enodeframework.common.extensions.NoOpMessageMonitorCallback;
import org.enodeframework.eventing.IDomainEvent;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Measures the difference in message timestamps between the last ingested and the last processed message.
 */
public class EventProcessorLatencyMonitor implements MessageMonitor<IDomainEvent<?>>, MetricSet {

    private final Clock clock;
    private final AtomicLong processTime = new AtomicLong();

    /**
     * Construct an {@link EventProcessorLatencyMonitor} using a {@link Clock#systemUTC()}.
     */
    public EventProcessorLatencyMonitor() {
        this(Clock.systemUTC());
    }

    /**
     * Construct an {@link EventProcessorLatencyMonitor} using the given {@code clock}.
     *
     * @param clock defines the {@link Clock} used by this {@link MessageMonitor} implementation
     */
    public EventProcessorLatencyMonitor(Clock clock) {
        this.clock = clock;
    }

    @Override
    public MonitorCallback onMessageIngested(IDomainEvent<?> message) {
        if (message != null) {
            this.processTime.set(Duration.between(message.getTimestamp().toInstant(), clock.instant()).toMillis());
        }
        return NoOpMessageMonitorCallback.INSTANCE;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("latency", (Gauge<Long>) processTime::get); // NOSONAR
        return metrics;
    }
}

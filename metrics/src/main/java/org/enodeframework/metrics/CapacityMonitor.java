package org.enodeframework.metrics;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Snapshot;
import org.enodeframework.common.extensions.MessageMonitor;
import org.enodeframework.messaging.IMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Calculates capacity by tracking, within the configured time window, the average message processing time
 * and multiplying that by the amount of messages processed.
 * <p>
 * The capacity can be more than 1 if the monitored
 * message handler processes the messages in parallel. The capacity for a single threaded message handler will be
 * a value between 0 and 1.
 * <p>
 * If the value for a single threaded message handler is 1 the component is active 100% of the time. This means
 * that messages will have to wait to be processed.
 */
public class CapacityMonitor implements MessageMonitor<IMessage>, MetricSet {

    private final Histogram processedDurationHistogram;
    private final TimeUnit timeUnit;
    private final long window;
    private final Clock clock;
    private final Metric capacity;

    /**
     * Creates a capacity monitor with the default time window 10 minutes
     */
    public CapacityMonitor() {
        this(10, TimeUnit.MINUTES);
    }

    /**
     * Creates a capacity monitor with the default time window 10 minutes
     *
     * @param window   The length of the window to measure the capacity over
     * @param timeUnit The time unit of the time window
     */
    public CapacityMonitor(long window, TimeUnit timeUnit) {
        this(window, timeUnit, Clock.defaultClock());
    }

    /**
     * Creates a capacity monitor with the given time window. Uses the provided clock
     * to measure process time per message.
     *
     * @param window   The length of the window to measure the capacity over
     * @param timeUnit The time unit of the time window
     * @param clock    The clock used to measure the process time per message
     */
    public CapacityMonitor(long window, TimeUnit timeUnit, Clock clock) {
        SlidingTimeWindowReservoir slidingTimeWindowReservoir = new SlidingTimeWindowReservoir(window, timeUnit, clock);
        this.processedDurationHistogram = new Histogram(slidingTimeWindowReservoir);
        this.timeUnit = timeUnit;
        this.window = window;
        this.clock = clock;
        this.capacity = new CapacityGauge();
    }

    @Override
    public MonitorCallback onMessageIngested(IMessage message) {
        final long start = clock.getTime();
        return new MonitorCallback() {
            @Override
            public void reportSuccess() {
                processedDurationHistogram.update(clock.getTime() - start);
            }

            @Override
            public void reportFailure(Throwable cause) {
                processedDurationHistogram.update(clock.getTime() - start);
            }

            @Override
            public void reportIgnored() {
                processedDurationHistogram.update(clock.getTime() - start);
            }
        };
    }

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("capacity", capacity);
        return metrics;
    }

    private class CapacityGauge implements Gauge<Double> {
        @Override
        public Double getValue() {
            Snapshot snapshot = processedDurationHistogram.getSnapshot();
            double meanProcessTime = snapshot.getMean();
            int numProcessed = snapshot.getValues().length;
            return (numProcessed * meanProcessTime) / timeUnit.toMillis(window);
        }
    }
}

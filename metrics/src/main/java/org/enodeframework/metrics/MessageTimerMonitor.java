package org.enodeframework.metrics;

import com.codahale.metrics.Clock;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Timer;
import org.enodeframework.common.extensions.MessageMonitor;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.messaging.IMessage;
import org.enodeframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A {@link MessageMonitor} which creates {@link Timer} instances for the overall, success, failure and ignored time an
 * ingested {@link Message} takes.
 */
public class MessageTimerMonitor implements MessageMonitor<IMessage>, MetricSet {

    private final Timer allTimer;
    private final Timer successTimer;
    private final Timer failureTimer;
    private final Timer ignoredTimer;

    /**
     * Instantiate a Builder to be able to create a {@link MessageTimerMonitor}.
     * <p>
     * The {@link Clock} is defaulted to a {@link Clock#defaultClock()} and the {@code reservoirFactory} defaults to
     * creating a {@link ExponentiallyDecayingReservoir}.
     *
     * @return a Builder to be able to create a {@link MessageTimerMonitor}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Instantiate a {@link MessageTimerMonitor} based on the fields contained in the {@link Builder}.
     *
     * @param builder the {@link Builder} used to instantiate a {@link MessageTimerMonitor} instance
     */
    protected MessageTimerMonitor(Builder builder) {
        builder.validate();

        Clock clock = builder.clock;
        Supplier<Reservoir> reservoirFactory = builder.reservoirFactory;

        allTimer = new Timer(reservoirFactory.get(), clock);
        successTimer = new Timer(reservoirFactory.get(), clock);
        failureTimer = new Timer(reservoirFactory.get(), clock);
        ignoredTimer = new Timer(reservoirFactory.get(), clock);
    }

    @Override
    public MonitorCallback onMessageIngested(IMessage message) {
        final Timer.Context allTimerContext = this.allTimer.time();
        final Timer.Context successTimerContext = this.successTimer.time();
        final Timer.Context failureTimerContext = this.failureTimer.time();
        final Timer.Context ignoredTimerContext = this.ignoredTimer.time();
        return new MessageMonitor.MonitorCallback() {
            @Override
            public void reportSuccess() {
                allTimerContext.stop();
                successTimerContext.stop();
            }

            @Override
            public void reportFailure(Throwable cause) {
                allTimerContext.stop();
                failureTimerContext.stop();
            }

            @Override
            public void reportIgnored() {
                allTimerContext.stop();
                ignoredTimerContext.stop();
            }
        };
    }

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("allTimer", allTimer);
        metrics.put("successTimer", successTimer);
        metrics.put("failureTimer", failureTimer);
        metrics.put("ignoredTimer", ignoredTimer);
        return metrics;
    }

    /**
     * Builder class to instantiate a {@link MessageTimerMonitor}.
     * <p>
     * The {@link Clock} is defaulted to a {@link Clock#defaultClock()} and the {@code reservoirFactory} defaults to
     * creating a {@link ExponentiallyDecayingReservoir}.
     */
    public static class Builder {

        private Clock clock = Clock.defaultClock();
        private Supplier<Reservoir> reservoirFactory = ExponentiallyDecayingReservoir::new;

        /**
         * Sets the {@link Clock} used to define the processing duration of a given message being pushed through this
         * {@link MessageMonitor}. Defaults to the {@link Clock#defaultClock}.
         *
         * @param clock the {@link Clock} used to define the processing duration of a given message
         * @return the current Builder instance, for fluent interfacing
         */
        public Builder clock(Clock clock) {
            Assert.nonNull(clock, "Clock may not be null");
            this.clock = clock;
            return this;
        }

        /**
         * Sets factory method creating a {@link Reservoir} to be used by the {@link Timer} instances created by this
         * {@link MessageMonitor}. Defaults to a {@link Supplier} of {@link ExponentiallyDecayingReservoir}.
         *
         * @param reservoirFactory a factory method creating a {@link Reservoir} to be used by the {@link Timer}
         *                         instances created by this {@link MessageMonitor}
         * @return the current Builder instance, for fluent interfacing
         */
        public Builder reservoirFactory(Supplier<Reservoir> reservoirFactory) {
            Assert.nonNull(reservoirFactory, "ReservoirFactory may not be null");
            this.reservoirFactory = reservoirFactory;
            return this;
        }

        /**
         * Initializes a {@link MessageTimerMonitor} as specified through this Builder.
         *
         * @return a {@link MessageTimerMonitor} as specified through this Builder
         */
        public MessageTimerMonitor build() {
            return new MessageTimerMonitor(this);
        }

        /**
         * Validate whether the fields contained in this Builder as set accordingly.
         * <p>
         * AxonConfigurationException if one field is asserted to be incorrect according to the Builder's
         * specifications
         */
        protected void validate() {
            // No assertions required, kept for overriding
        }
    }
}

package org.enodeframework.test.eventhandler;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.test.domain.TestAggregateCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Event
public class TestAggregateEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(TestAggregateEventHandler.class);

    @Subscribe
    public void handleAsync(TestAggregateCreated evnt) {
        logger.info("TestAggregateCreated evnt executed:{}", evnt.title);

    }
}

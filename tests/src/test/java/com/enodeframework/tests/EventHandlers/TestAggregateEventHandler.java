package com.enodeframework.tests.EventHandlers;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.tests.Domain.TestAggregateCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Event
public class TestAggregateEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(TestAggregateEventHandler.class);

    @Subscribe
    public AsyncTaskResult HandleAsync(TestAggregateCreated evnt) {
        logger.info("TestAggregateCreated evnt executed:{}", evnt.Title);
        return AsyncTaskResult.Success;
    }
}

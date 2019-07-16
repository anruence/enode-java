package org.enodeframework.tests.eventhandlers;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Priority;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.tests.EnodeCoreTest;
import org.enodeframework.tests.domain.Event1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Priority(3)
@Event
public class Handler2 {
    private Logger logger = LoggerFactory.getLogger(Handler2.class);

    @Subscribe
    public void handleAsync(Event1 evnt) {
        logger.info("event1 handled by handler2.");
        EnodeCoreTest.HandlerTypes.computeIfAbsent(1, k -> new ArrayList<>()).add(getClass().getName());
    }
}

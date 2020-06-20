package org.enodeframework.tests.eventhandlers;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Priority;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.tests.domain.Event1;
import org.enodeframework.tests.EnodeCoreTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Priority(1)
@Event
public class Handler1 {
    private Logger logger = LoggerFactory.getLogger(Handler1.class);

    @Priority(4)
    @Subscribe
    public void HandleAsync(Event1 evnt) {
        logger.info("event1 handled by handler1.");
        EnodeCoreTest.HandlerTypes.computeIfAbsent(1, k -> new ArrayList<>()).add(getClass().getName());
    }
}

package org.enodeframework.test.eventhandler;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Priority;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.test.EnodeCoreTest;
import org.enodeframework.test.domain.Event1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Priority(2)
@Event
public class Handler3 {
    private Logger logger = LoggerFactory.getLogger(Handler3.class);

    @Subscribe
    public void handleAsync(Event1 evnt) {
        logger.info("event1 handled by handler3.");
        EnodeCoreTest.HandlerTypes.computeIfAbsent(1, k -> new ArrayList<>()).add(getClass().getName());

    }
}

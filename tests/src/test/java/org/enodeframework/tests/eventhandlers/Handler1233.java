package org.enodeframework.tests.eventhandlers;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Priority;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.tests.domain.Event1;
import org.enodeframework.tests.domain.Event2;
import org.enodeframework.tests.domain.Event3;
import org.enodeframework.tests.EnodeCoreTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Priority(1)
@Event
public class Handler1233 {
    private Logger _logger = LoggerFactory.getLogger(Handler123.class);

    @Priority(4)
    @Subscribe
    public void HandleAsync(Event1 evnt, Event2 evnt2, Event3 evnt3) {
        _logger.info("event1,event2,event3 handled by handler3.");
        EnodeCoreTest.HandlerTypes.computeIfAbsent(3, k -> new ArrayList<>()).add(getClass().getName());

    }
}
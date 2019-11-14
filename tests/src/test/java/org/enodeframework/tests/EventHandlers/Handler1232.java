package org.enodeframework.tests.EventHandlers;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Priority;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.tests.Domain.Event1;
import org.enodeframework.tests.Domain.Event2;
import org.enodeframework.tests.Domain.Event3;
import org.enodeframework.tests.TestClasses.CommandAndEventServiceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Priority(2)
@Event
public class Handler1232 {
    private Logger _logger = LoggerFactory.getLogger(Handler123.class);

    @Subscribe
    public void HandleAsync(Event1 evnt, Event2 evnt2, Event3 evnt3) {
        _logger.info("event1,event2,event3 handled by handler2.");
        CommandAndEventServiceTest.HandlerTypes.computeIfAbsent(3, k -> new ArrayList<>()).add(getClass().getName());

    }
}
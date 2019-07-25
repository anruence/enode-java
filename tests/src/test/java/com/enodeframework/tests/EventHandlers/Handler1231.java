package com.enodeframework.tests.EventHandlers;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Priority;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.tests.Domain.Event1;
import com.enodeframework.tests.Domain.Event2;
import com.enodeframework.tests.Domain.Event3;
import com.enodeframework.tests.TestClasses.CommandAndEventServiceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Priority(3)
@Event
public class Handler1231 {
    private Logger _logger = LoggerFactory.getLogger(Handler123.class);

    @Subscribe
    public AsyncTaskResult HandleAsync(Event1 evnt, Event2 evnt2, Event3 evnt3) {
        _logger.info("event1,event2,event3 handled by handler1.");
        CommandAndEventServiceTest.HandlerTypes.computeIfAbsent(3, k -> new ArrayList<>()).add(getClass().getName());
        return AsyncTaskResult.Success;
    }
}
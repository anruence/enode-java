package com.enodeframework.tests.EventHandlers;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Priority;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.tests.Domain.Event1;
import com.enodeframework.tests.TestClasses.CommandAndEventServiceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Priority(2)
@Event
public class Handler3 {
    private Logger logger = LoggerFactory.getLogger(Handler3.class);

    @Subscribe
    public AsyncTaskResult HandleAsync(Event1 evnt) {
        logger.info("event1 handled by handler3.");
        CommandAndEventServiceTest.HandlerTypes.computeIfAbsent(1, k -> new ArrayList<>()).add(getClass().getName());
        return AsyncTaskResult.Success;
    }
}

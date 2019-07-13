package com.enodeframework.tests.EventHandlers;


import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Priority;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.tests.Domain.Event1;
import com.enodeframework.tests.Domain.Event2;
import com.enodeframework.tests.Domain.Event3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Priority
@Event
public class Handler1233 {

    private Logger _logger = LoggerFactory.getLogger(Handler123.class);

    @Priority(4)
    @Subscribe
    public AsyncTaskResult HandleAsync(Event1 evnt, Event2 evnt2, Event3 evnt3) {
        _logger.info("event1,event2,event3 handled by handler3.");
//        CommandAndEventServiceTest.HandlerTypes.AddOrUpdate(3,
//                x = > new List<String> {
//            GetType().Name
//        },
//        (x, existing) =>
//        {
//            existing.Add(GetType().Name);
//            return existing;
//        });
        return AsyncTaskResult.Success;
    }
}
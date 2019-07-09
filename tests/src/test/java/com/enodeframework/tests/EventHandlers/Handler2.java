package com.enodeframework.tests.EventHandlers;

import com.enodeframework.tests.Domain.Event1;
import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Priority;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;

@Priority(1)
@Event
public class Handler2 {
    @Priority(4)
    @Subscribe
    public AsyncTaskResult HandleAsync(Event1 evnt) {
//        _logger.Info("event1 handled by handler1.");
//        CommandAndEventServiceTest.HandlerTypes.AddOrUpdate(1,
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

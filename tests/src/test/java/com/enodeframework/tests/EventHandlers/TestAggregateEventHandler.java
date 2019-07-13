package com.enodeframework.tests.EventHandlers;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.tests.Domain.TestAggregateCreated;


@Event
public class TestAggregateEventHandler {

    @Subscribe
    public AsyncTaskResult HandleAsync(TestAggregateCreated evnt) {
        System.out.println("this was executed" + evnt.Title);
        //DO NOTHING
        return AsyncTaskResult.Success;
    }
}

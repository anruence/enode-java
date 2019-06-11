package com.enodeframework.samples.eventhandlers.note;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.samples.domain.note.NoteTitleChanged;
import com.enodeframework.samples.domain.note.NoteTitleChanged2;

@Event
public class NoteEventHandler {

    @Subscribe
    public AsyncTaskResult handleAsync(NoteTitleChanged evnt) {
        System.out.println(String.format("Note denormalizered, title：%s, Version: %d", evnt.getTitle(), evnt.version()));
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(NoteTitleChanged2 evnt) {
        System.out.println(String.format("Note denormalizered1, title：%s, Version: %d", evnt.getTitle(), evnt.version()));
        return (AsyncTaskResult.Success);
    }
}

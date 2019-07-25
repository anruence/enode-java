package com.enodeframework.samples.eventhandlers.note;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.samples.domain.note.NoteCreated;
import com.enodeframework.samples.domain.note.NoteTitleChanged;
import com.enodeframework.samples.domain.note.NoteTitleChanged2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Event
public class NoteEventHandler {
    public static Logger logger = LoggerFactory.getLogger(NoteEventHandler.class);

    @Subscribe
    public AsyncTaskResult handleAsync(NoteTitleChanged evnt) {
        logger.info("NoteTitleChanged Note denormalizered, title：{}, Version: {}", evnt.getTitle(), evnt.getVersion());
        return AsyncTaskResult.Success;
    }

    @Subscribe
    public AsyncTaskResult handleAsync(NoteCreated evnt) {
        logger.info("NoteCreated title：{}, Version: {}", evnt.getTitle(), evnt.getVersion());
        return AsyncTaskResult.Success;
    }

    @Subscribe
    public AsyncTaskResult handleAsync(NoteTitleChanged2 evnt) {
        logger.info("NoteTitleChanged2 Note denormalizered, title：{}, Version: {}", evnt.getTitle(), evnt.getVersion());
        return AsyncTaskResult.Success;
    }
}

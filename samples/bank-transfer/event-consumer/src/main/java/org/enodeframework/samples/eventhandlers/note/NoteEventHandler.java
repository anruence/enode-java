package org.enodeframework.samples.eventhandlers.note;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.samples.domain.note.NoteCreated;
import org.enodeframework.samples.domain.note.NoteTitleChanged;
import org.enodeframework.samples.domain.note.NoteTitleChanged2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Event
public class NoteEventHandler {
    public static Logger logger = LoggerFactory.getLogger(NoteEventHandler.class);

    @Subscribe
    public void handleAsync(NoteTitleChanged evnt) {
        logger.info("NoteTitleChanged Note denormalizered, title：{}, Version: {},endTime:{}", evnt.getTitle(), evnt.getVersion(), System.currentTimeMillis());
    }

    @Subscribe
    public void handleAsync(NoteCreated evnt) {
        logger.info("NoteCreated title：{}, Version: {},endTime:{}", evnt.getTitle(), evnt.getVersion(), System.currentTimeMillis());
    }

    @Subscribe
    public void handleAsync(NoteTitleChanged2 evnt) {
        logger.info("NoteTitleChanged2 Note denormalizered, title：{}, Version: {}", evnt.getTitle(), evnt.getVersion());
    }
}

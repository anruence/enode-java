package com.enodeframework.samples.commandhandles.note;

import com.enodeframework.annotation.Command;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.common.io.Task;
import com.enodeframework.samples.commands.note.ChangeNoteTitleCommand;
import com.enodeframework.samples.domain.note.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@Command
public class ChangeNoteTitleCommandHandler {
    private Logger logger = LoggerFactory.getLogger(ChangeNoteTitleCommandHandler.class);

    @Subscribe
    public void handleAsync(ICommandContext context, ChangeNoteTitleCommand command) {
        logger.info(command.getTitle());
        CompletableFuture<Note> future = context.getAsync(command.getAggregateRootId(), false, Note.class);
        Note note = Task.get(future);
        logger.info("note:{}", note.id());
        note.changeTitle(command.getTitle());
    }
}

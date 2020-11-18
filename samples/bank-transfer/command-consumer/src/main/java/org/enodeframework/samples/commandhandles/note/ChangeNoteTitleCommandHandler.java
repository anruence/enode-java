package org.enodeframework.samples.commandhandles.note;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.common.io.Task;
import org.enodeframework.samples.commands.note.ChangeNoteTitleCommand;
import org.enodeframework.samples.domain.note.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@Command
public class ChangeNoteTitleCommandHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(ChangeNoteTitleCommandHandler.class);

    @Subscribe
    public void handleAsync(ICommandContext context, ChangeNoteTitleCommand command) {
        CompletableFuture<Note> future = context.getAsync(command.getAggregateRootId(), true, Note.class);
        Note note = Task.await(future);
        note.changeTitle(command.getTitle());
    }
}

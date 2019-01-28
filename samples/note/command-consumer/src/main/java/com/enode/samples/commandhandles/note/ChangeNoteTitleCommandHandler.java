package com.enode.samples.commandhandles.note;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.common.logging.ENodeLogger;
import com.enode.samples.commands.note.ChangeNoteTitleCommand;
import com.enode.samples.domain.note.Note;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class ChangeNoteTitleCommandHandler implements ICommandHandler<ChangeNoteTitleCommand> {
    private Logger logger = ENodeLogger.getLog();

    @Override
    public CompletableFuture<Note> handleAsync(ICommandContext context, ChangeNoteTitleCommand command) {
        logger.info(command.getTitle());
        CompletableFuture<Note> noteCompletableFuture = context.getAsync(command.getAggregateRootId(), true, Note.class);
        return noteCompletableFuture.thenApply(note -> {
            logger.info("note:{}", note.toString());
            note.changeTitle(command.getTitle());
            return note;
        });
    }
}

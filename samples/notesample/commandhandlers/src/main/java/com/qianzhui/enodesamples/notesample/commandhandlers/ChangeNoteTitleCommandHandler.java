package com.qianzhui.enodesamples.notesample.commandhandlers;

import com.qianzhui.enode.commanding.ICommandContext;
import com.qianzhui.enode.commanding.ICommandHandler;
import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enodesamples.notesample.commands.ChangeNoteTitleCommand;
import com.qianzhui.enodesamples.notesample.domain.Note;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class ChangeNoteTitleCommandHandler implements ICommandHandler<ChangeNoteTitleCommand> {
    private Logger logger = ENodeLogger.getLog();

    @Override
    public CompletableFuture handleAsync(ICommandContext context, ChangeNoteTitleCommand command) {
        logger.info(command.getTitle());
        CompletableFuture<Note> noteCompletableFuture = context.getAsync(command.getAggregateRootId(), true, Note.class);
        return noteCompletableFuture.thenApply(note -> {
            logger.info("note", note.toString());
            note.changeTitle(command.getTitle());
            return note;
        });
    }
}

package com.enode.samples.commandhandles.note;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.common.logging.ENodeLogger;
import com.enode.samples.commands.note.CreateNoteCommand;
import com.enode.samples.domain.note.Note;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class CreateNoteCommandHandler implements ICommandHandler<CreateNoteCommand> {

    private static Logger logger = ENodeLogger.getLog();

    /**
     * Handle the given aggregate command.
     *
     * @param context
     * @param command
     * @return
     */
    @Override
    public CompletableFuture handleAsync(ICommandContext context, CreateNoteCommand command) {
        Note note = new Note(command.getAggregateRootId(), command.getTitle());
        context.add(note);
        return CompletableFuture.completedFuture(note);
    }

}

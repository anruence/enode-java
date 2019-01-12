package com.qianzhui.enodesamples.notesample.commandhandlers;

import com.qianzhui.enode.commanding.ICommandContext;
import com.qianzhui.enode.commanding.ICommandHandler;
import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enodesamples.notesample.commands.CreateNoteCommand;
import com.qianzhui.enodesamples.notesample.domain.Note;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

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
        context.add(new Note(command.getAggregateRootId(), command.getTitle()));
        return null;
    }

}

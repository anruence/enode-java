package com.enodeframework.samples.commandhandles.note;

import com.enodeframework.annotation.Command;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.samples.commands.note.CreateNoteCommand;
import com.enodeframework.samples.domain.note.Note;

@Command
public class CreateNoteCommandHandler {
    /**
     * Handle the given aggregate command.
     *
     * @param context
     * @param command
     * @return
     */
    @Subscribe
    public void handleAsync(ICommandContext context, CreateNoteCommand command) {
        Note note = new Note(command.getAggregateRootId(), command.getTitle());
        context.add(note);
    }
}

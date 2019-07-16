package org.enodeframework.samples.commandhandles.note;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.samples.commands.note.CreateNoteCommand;
import org.enodeframework.samples.domain.note.Note;

@Command
public class CreateNoteCommandHandler {
    /**
     * Handle the given aggregate command.
     */
    @Subscribe
    public void handleAsync(ICommandContext context, CreateNoteCommand command) {
        Note note = new Note(command.getAggregateRootId(), command.getTitle());
        context.add(note);
    }
}

package org.enodeframework.samples.commandhandles.note

import kotlinx.coroutines.future.asDeferred
import org.enodeframework.annotation.Command
import org.enodeframework.annotation.Subscribe
import org.enodeframework.commanding.ICommandContext
import org.enodeframework.samples.commands.note.ChangeNoteTitleCommand
import org.enodeframework.samples.domain.note.Note

@Command
class ChangeNoteTitleCommandHandler {
    @Subscribe
    suspend fun handleAsync(context: ICommandContext, command: ChangeNoteTitleCommand) {
        val note = context.getAsync(command.getAggregateRootId(), true, Note::class.java).asDeferred().await()
        note.changeTitle(command.title)
    }
}
package com.enodeframework.samples.controller.note;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.commanding.ICommandService;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.Task;
import com.enodeframework.samples.commands.note.ChangeNoteTitleCommand;
import com.enodeframework.samples.commands.note.CreateNoteCommand;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private ICommandService commandService;

    @RequestMapping("create")
    public Object create(@RequestParam("id") String noteId, @RequestParam("t") String title, @RequestParam(value = "c", required = false) String cid) {
        if (!Strings.isNullOrEmpty(cid)) {
            createNoteCommand.setId(cid);
        }
        CreateNoteCommand createNoteCommand = new CreateNoteCommand(noteId, title);
        AsyncTaskResult<CommandResult> asyncTaskResult = commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        Assert.notNull(asyncTaskResult, "asyncTaskResult");
        commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        ChangeNoteTitleCommand titleCommand = new ChangeNoteTitleCommand(noteId, title + " change");
        // always block here
        Task.get(commandService.executeAsync(titleCommand, CommandReturnType.EventHandled));
        return null;
    }

    @RequestMapping("change")
    public Object change(@RequestParam("id") String noteId, @RequestParam("t") String title, @RequestParam(value = "c", required = false) String cid) {
        ChangeNoteTitleCommand titleCommand = new ChangeNoteTitleCommand(noteId, title);
        if (!Strings.isNullOrEmpty(cid)) {
            titleCommand.setId(cid);
        }
        return Task.get(commandService.executeAsync(titleCommand, CommandReturnType.EventHandled));
    }
}

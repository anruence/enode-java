package com.qianzhui.enodesamples.quickstart.controller;

import com.qianzhui.enode.commanding.CommandResult;
import com.qianzhui.enode.commanding.CommandReturnType;
import com.qianzhui.enode.commanding.ICommandService;
import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.common.utilities.CompletableFutureUtil;
import com.qianzhui.enodesamples.notesample.commands.ChangeNoteTitleCommand;
import com.qianzhui.enodesamples.notesample.commands.CreateNoteCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private ICommandService commandService;

    @RequestMapping("create")
    public Object create(@RequestParam("id") String noteId, @RequestParam("t") String title) {
        CreateNoteCommand command1 = new CreateNoteCommand(noteId, title);
        AsyncTaskResult<CommandResult> promise = CompletableFutureUtil.getValue(commandService.executeAsync(command1, CommandReturnType.EventHandled));
        return promise;
    }

    @RequestMapping("change")
    public Object change(@RequestParam("id") String noteId, @RequestParam("t") String title) throws ExecutionException, InterruptedException {
        ChangeNoteTitleCommand command2 = new ChangeNoteTitleCommand(noteId, title);
        AsyncTaskResult<CommandResult> promise = CompletableFutureUtil.getValue(commandService.executeAsync(command2, CommandReturnType.EventHandled));
        return promise;
    }
}

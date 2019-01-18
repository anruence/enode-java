package com.enode.samples.controller;

import com.enode.commanding.CommandResult;
import com.enode.commanding.CommandReturnType;
import com.enode.commanding.ICommandService;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.utilities.CompletableFutureUtil;
import com.enode.samples.commands.ChangeNoteTitleCommand;
import com.enode.samples.commands.CreateNoteCommand;
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

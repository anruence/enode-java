package com.enode.samples.controller.note;

import com.enode.commanding.CommandResult;
import com.enode.commanding.CommandReturnType;
import com.enode.common.io.AsyncTaskResult;
import com.enode.rocketmq.message.RocketMQCommandService;
import com.enode.samples.commands.note.ChangeNoteTitleCommand;
import com.enode.samples.commands.note.CreateNoteCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    RocketMQCommandService commandService;

    @RequestMapping("create")
    public Object create(@RequestParam("id") String noteId, @RequestParam("t") String title, @RequestParam("c") String cid) {
        CreateNoteCommand command1 = new CreateNoteCommand(noteId, title);
        command1.setId(cid);
        AsyncTaskResult<CommandResult> promise = CompletableFutureUtil.getValue(commandService.executeAsync(command1, CommandReturnType.EventHandled));
        return promise;
    }

    @RequestMapping("change")
    public Object change(@RequestParam("id") String noteId, @RequestParam("t") String title, @RequestParam("c") String cid) {
        ChangeNoteTitleCommand command2 = new ChangeNoteTitleCommand(noteId, title);
        command2.setId(cid);
        AsyncTaskResult<CommandResult> promise = CompletableFutureUtil.getValue(commandService.executeAsync(command2, CommandReturnType.EventHandled));
        return promise;
    }
}

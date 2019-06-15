package com.enodeframework.samples.controller.note;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.Task;
import com.enodeframework.rocketmq.message.RocketMQCommandService;
import com.enodeframework.samples.commands.note.ChangeNoteTitleCommand;
import com.enodeframework.samples.commands.note.CreateNoteCommand;
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
        AsyncTaskResult<CommandResult> promise = Task.get(commandService.executeAsync(command1, CommandReturnType.EventHandled));
        return promise;
    }

    @RequestMapping("change")
    public Object change(@RequestParam("id") String noteId, @RequestParam("t") String title, @RequestParam("c") String cid) {
        ChangeNoteTitleCommand command2 = new ChangeNoteTitleCommand(noteId, title);
        command2.setId(cid);
        AsyncTaskResult<CommandResult> promise = Task.get(commandService.executeAsync(command2, CommandReturnType.EventHandled));
        return promise;
    }
}

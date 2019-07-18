package com.enodeframework.samples.controller.note;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.commanding.ICommandService;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.Task;
import com.enodeframework.common.utilities.ObjectId;
import com.enodeframework.samples.commands.note.ChangeNoteTitleCommand;
import com.enodeframework.samples.commands.note.CreateNoteCommand;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private ICommandService commandService;

    @RequestMapping("create")
    public Object create(@RequestParam("id") String noteId, @RequestParam("t") String title, @RequestParam(value = "c", required = false) String cid) {
        CreateNoteCommand createNoteCommand = new CreateNoteCommand(noteId, title);
        if (!Strings.isNullOrEmpty(cid)) {
            createNoteCommand.setId(cid);
        }
        AsyncTaskResult<CommandResult> asyncTaskResult = commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        Assert.notNull(asyncTaskResult, "asyncTaskResult");
        commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled).join();
        ChangeNoteTitleCommand titleCommand = new ChangeNoteTitleCommand(noteId, title + " change");
        // always block here
        return Task.get(commandService.executeAsync(titleCommand, CommandReturnType.EventHandled));
    }

    @RequestMapping("test")
    public Object test(@RequestParam("id") int totalCount) throws InterruptedException {
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(totalCount);
        for (int i = 0; i < totalCount; i++) {
            CreateNoteCommand command = new CreateNoteCommand(ObjectId.generateNewStringId(), "Sample Note" + ObjectId.generateNewStringId());
            command.setId(String.valueOf(i));
            try {
                commandService.executeAsync(command).thenAccept(result -> {
                    latch.countDown();
                });
            } catch (Exception e) {
                latch.countDown();
            }
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("run duration " + (end - start));
        return end - start;
    }
}

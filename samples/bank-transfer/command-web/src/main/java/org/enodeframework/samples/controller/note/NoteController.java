package org.enodeframework.samples.controller.note;

import com.google.common.base.Strings;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.samples.commands.note.ChangeNoteTitleCommand;
import org.enodeframework.samples.commands.note.CreateNoteCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
        return Task.await(commandService.executeAsync(titleCommand, CommandReturnType.EventHandled));
    }

    @RequestMapping("test")
    public Map test(@RequestParam("id") int totalCount,
                    @RequestParam(required = false, name = "mode", defaultValue = "0") int mode) throws Exception {
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(totalCount);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        for (int i = 0; i < totalCount; i++) {
            CreateNoteCommand command = new CreateNoteCommand(ObjectId.generateNewStringId(), "Sample Note" + ObjectId.generateNewStringId());
            command.setId(String.valueOf(i));
            try {
                CompletableFuture future;
                if (mode == 1) {
                    future = commandService.executeAsync(command, CommandReturnType.EventHandled);
                } else if (mode == 2) {
                    future = commandService.executeAsync(command, CommandReturnType.CommandExecuted);
                } else {
                    future = commandService.sendAsync(command);
                }
                future.thenAccept(result -> {
                    if (((AsyncTaskResult) result).getStatus() == AsyncTaskStatus.Success) {
                        success.incrementAndGet();
                    } else {
                        failed.incrementAndGet();
                    }
                    latch.countDown();
                });
            } catch (Exception e) {
                latch.countDown();
            }
        }
        latch.await();
        long end = System.currentTimeMillis();
        Map map = new HashMap();
        map.put("time", end - start);
        map.put("success", success.get());
        map.put("failed", failed.get());
        return map;
    }
}

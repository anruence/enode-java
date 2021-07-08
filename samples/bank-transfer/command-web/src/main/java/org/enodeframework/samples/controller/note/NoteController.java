package org.enodeframework.samples.controller.note;

import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.utils.IdGenerator;
import org.enodeframework.samples.commands.note.ChangeNoteTitleCommand;
import org.enodeframework.samples.commands.note.CreateNoteCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private ICommandService commandService;

    @RequestMapping("create")
    public Mono create(@RequestParam("id") String noteId, @RequestParam("t") String title) {
        CreateNoteCommand createNoteCommand = new CreateNoteCommand(noteId, title);
        Mono mono = Mono.fromFuture(commandService.executeAsync(createNoteCommand, CommandReturnType.EventHandled));
        return mono;
    }

    @RequestMapping("update")
    public Mono update(@RequestParam("id") String noteId, @RequestParam("t") String title) {
        ChangeNoteTitleCommand titleCommand = new ChangeNoteTitleCommand(noteId, title + " change");
        Mono mono = Mono.fromFuture(commandService.executeAsync(titleCommand, CommandReturnType.EventHandled));
        return mono;
    }

    @RequestMapping("test")
    public Map test(@RequestParam("id") int totalCount, @RequestParam(required = false, name = "mode", defaultValue = "0") int mode) throws Exception {
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(totalCount);
        for (int i = 0; i < totalCount; i++) {
            CreateNoteCommand command = new CreateNoteCommand(IdGenerator.nextId(), "Sample Note" + IdGenerator.nextId());
            command.setId(String.valueOf(i));
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
        return map;
    }
}

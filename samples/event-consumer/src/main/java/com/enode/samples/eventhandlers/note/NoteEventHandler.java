package com.enode.samples.eventhandlers.note;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.note.NoteTitleChanged;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class NoteEventHandler implements IMessageHandler<NoteTitleChanged> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(NoteTitleChanged evnt) {
        System.out.println(String.format("Note denormalizered, title：%s, Version: %d", evnt.getTitle(), evnt.version()));
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}

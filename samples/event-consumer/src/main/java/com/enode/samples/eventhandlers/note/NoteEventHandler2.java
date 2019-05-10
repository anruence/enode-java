package com.enode.samples.eventhandlers.note;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.ITwoMessageHandler;
import com.enode.samples.domain.note.NoteTitleChanged;
import com.enode.samples.domain.note.NoteTitleChanged2;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class NoteEventHandler2 implements ITwoMessageHandler<NoteTitleChanged, NoteTitleChanged2> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(NoteTitleChanged evnt, NoteTitleChanged2 evnt2) {
        System.out.println(String.format("Note denormalizered, title：%s, Version: %d", evnt2.getTitle(), evnt.version()));
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}

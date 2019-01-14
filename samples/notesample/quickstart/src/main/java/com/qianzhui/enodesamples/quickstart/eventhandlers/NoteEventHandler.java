package com.qianzhui.enodesamples.quickstart.eventhandlers;

import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.infrastructure.IMessageHandler;
import com.qianzhui.enodesamples.notesample.domain.NoteTitleChanged;

import java.util.concurrent.CompletableFuture;

public class NoteEventHandler implements IMessageHandler<NoteTitleChanged> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(NoteTitleChanged evnt) {
        System.out.println(String.format("Note denormalizered, title：%s, Version: %d", evnt.getTitle(), evnt.version()));
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}

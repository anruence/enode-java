package com.enode.samples.eventhandlers;

import com.enode.commanding.ICommandService;
import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.infrastructure.ITwoMessageHandler;
import com.enode.samples.domain.NoteTitleChanged;
import com.enode.samples.domain.NoteTitleChanged2;

import java.util.concurrent.CompletableFuture;

public class NoteEventHandler2 implements ITwoMessageHandler<NoteTitleChanged, NoteTitleChanged2> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(NoteTitleChanged evnt, NoteTitleChanged2 evnt2) {
        System.out.println(String.format("Note denormalizered, title：%s, Version: %d", evnt2.getTitle(), evnt.version()));
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}

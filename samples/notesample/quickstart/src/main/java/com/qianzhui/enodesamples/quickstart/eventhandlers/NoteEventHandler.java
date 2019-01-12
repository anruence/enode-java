package com.qianzhui.enodesamples.quickstart.eventhandlers;

import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.infrastructure.IMessageHandler;
import com.qianzhui.enodesamples.notesample.domain.NoteTitleChanged;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class NoteEventHandler implements IMessageHandler<NoteTitleChanged> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(NoteTitleChanged evnt) {
        System.out.println(String.format("Note denormalizered, title：%s, Version: %d", evnt.getTitle(), evnt.version()));
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> c1 = CompletableFuture.supplyAsync(() -> {
            return 1;
        });

        CompletableFuture c2 = c1.thenApply(c->{
            return c+5;
        });
        System.out.print(c1.get());
        System.out.print(c2.get());

        CompletableFuture<String> future = c1.thenCompose(p -> {
            return CompletableFuture.supplyAsync(() -> {
                if (p < 10) {
                    return "22";
                }
                return "33" + p;
            });
        });
        System.out.print(future.get());
    }
}

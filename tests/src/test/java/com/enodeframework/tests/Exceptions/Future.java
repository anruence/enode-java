package com.enodeframework.tests.Exceptions;

import com.enodeframework.common.exception.IORuntimeException;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class Future {


    @Test
    public void test_can_throw_in_future() throws Exception {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            throw new IORuntimeException("test exception");
            return 2;
        });

        future.thenAccept(r->{
            int a = 1;
            return;
        }).exceptionally(e -> {
            if (e.getCause() instanceof IORuntimeException) {
                e.printStackTrace();
            } else {

            }
            return null;
        }).handleAsync((r,e)->{
            if (e != null){

            }
            if (r != null) {
//            r.getClass();
            }
            e.printStackTrace();
            return null;
        }).thenAccept(r->{
            int a = 1;
            return;
        });
    }
}

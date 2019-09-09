package org.enodeframework.tests.Exceptions;

import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class FutureException {

    @Test
    public void test_can_throw_in_future() throws Exception {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        future = CompletableFuture.supplyAsync(() -> {
            throw new IORuntimeException("test exception");
        });
        try {
            future.thenAccept(x -> {
                throw new ENodeRuntimeException("ss");
            });
        } catch (Exception e) {
            // 此时catch不到异常
            e.printStackTrace();
        }
        future.exceptionally(e -> {
            if (e.getCause() instanceof IORuntimeException) {
                System.out.println("===IO");
                e.printStackTrace();
            } else {
                System.out.println("===NO");
                e.printStackTrace();
            }
            return null;
        }).handleAsync((r, e) -> {
            if (e != null) {
                e.printStackTrace();
            }
            if (r != null) {
                System.out.println(r.getClass().getName());
            }
            return null;
        }).thenAccept(r -> {
            // 执行结果
            System.out.println("RET" + r);
        });
    }
}

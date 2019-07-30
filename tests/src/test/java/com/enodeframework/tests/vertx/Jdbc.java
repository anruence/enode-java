package com.enodeframework.tests.vertx;

import com.enodeframework.common.io.Task;
import com.enodeframework.tests.TestClasses.AbstractTest;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.JDBCClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Jdbc extends AbstractTest {

    @Autowired
    DataSource ds;

    @Test
    public void block() {
        Vertx vertx = Vertx.vertx();
        JDBCClient client = JDBCClient.create(vertx, ds);
        CompletableFuture future = new CompletableFuture();
        client.query("select * from eventStream limit 1", x -> {
            if (x.succeeded()) {
                future.complete(true);
                System.out.println(x.result());
            } else {
                future.completeExceptionally(x.cause());
            }
            CompletableFuture future1 = new CompletableFuture();
            client.query("select * from eventStream limit 1", xx -> {
                if (xx.succeeded()) {
                    future1.complete(true);
                    System.out.println(xx.result());
                } else {
                    future1.completeExceptionally(xx.cause());
                }
            });
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return;
        });

        Task.sleep(10000000);
    }
}

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

public class VertxJdbc extends AbstractTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void block() {
        Vertx vertx = Vertx.vertx();
        JDBCClient client = JDBCClient.create(vertx, dataSource);
        CompletableFuture future1 = new CompletableFuture();
        client.query("select * from eventStream limit 1", x -> {
            if (x.succeeded()) {
                future1.complete(true);
                System.out.println(x.result());
            } else {
                future1.completeExceptionally(x.cause());
            }
            CompletableFuture future2 = new CompletableFuture();
            client.query("select * from eventStream limit 1", xx -> {
                if (xx.succeeded()) {
                    future2.complete(true);
                    System.out.println(xx.result());
                } else {
                    future2.completeExceptionally(xx.cause());
                }
            });
            try {
                //EventLoop thread always block here
                future2.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        Task.sleep(100000);
    }
}

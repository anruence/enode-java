package com.enodeframework.tests.vertx;

import com.enodeframework.common.SysProperties;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SocketClient {

    public static Logger logger = LoggerFactory.getLogger(SocketClient.class);

    public static ConcurrentHashMap<String, CompletableFuture<NetSocket>> smap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        NetClient client = Vertx.vertx().createNetClient();
        String host = "127.0.0.1";
        long start = System.currentTimeMillis();
        int total = 100000;
        CompletableFuture<NetSocket> future = new CompletableFuture<>();
        if (smap.putIfAbsent(host, future) == null) {
            client.connect(6008, host, socketAsyncResult -> {
                if (socketAsyncResult.succeeded()) {
                    NetSocket socket = socketAsyncResult.result();
                    socket.closeHandler(x -> {
                        smap.remove(host);
                    }).endHandler(x -> {
                        smap.remove(host);
                        logger.info("end:{}", x);
                    });
                    future.complete(socket);
                }
            });
        }
        smap.get(host).thenAccept(socket -> {
            for (int i = 0; i < total; i++) {
                int ii = i;
                socket.write("send message:" + ii + SysProperties.DELIMITED);
            }
        });

        long end = System.currentTimeMillis();
        logger.info("time:{}", end - start);
        System.in.read();
    }
}

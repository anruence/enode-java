package com.enodeframework.tests.vertx;

import com.enodeframework.common.SysProperties;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class SocketClient {

    public static Logger logger = LoggerFactory.getLogger(SocketClient.class);

    public static ConcurrentHashMap<String, NetSocket> smap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        NetClient client = Vertx.vertx().createNetClient();
        String host = "127.0.0.1";
        long start = System.currentTimeMillis();
        int total = 100;
        CountDownLatch connectLatch = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(total);
        for (int i = 0; i < total; i++) {
            int ii = i;
            if (smap.containsKey(host)) {
                NetSocket socket = smap.get(host);
                socket.write("send message:" + ii + SysProperties.DELIMITED);
                latch.countDown();
                continue;
            }
            client.connect(6008, host, socketAsyncResult -> {
                if (socketAsyncResult.succeeded()) {
                    connectLatch.countDown();
                    NetSocket socket = socketAsyncResult.result();
                    smap.put(host, socket);
                    socket.closeHandler(x -> {
                        smap.remove(host);
                    }).endHandler(x -> {
                        smap.remove(host);
                        logger.info("end:{}{}", ii, x);
                    });
                    socket.write("send message:" + ii + SysProperties.DELIMITED);
                    latch.countDown();
                }
            });
            connectLatch.await();
        }
        latch.await();
        long end = System.currentTimeMillis();
        logger.info("time:{}", end - start);
        System.in.read();
    }
}

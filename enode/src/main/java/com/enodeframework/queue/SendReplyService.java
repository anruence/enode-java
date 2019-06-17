package com.enodeframework.queue;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.common.utilities.RemoteReply;
import com.enodeframework.common.utilities.RemotingUtil;
import com.enodeframework.queue.domainevent.DomainEventHandledMessage;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SendReplyService {

    private static final Logger logger = LoggerFactory.getLogger(SendReplyService.class);
    private final ConcurrentMap<String, NetClient> clientTables = new ConcurrentHashMap<>();
    private boolean started;
    private boolean stoped;
    private Vertx vertx;

    public void start() {
        if (!started) {
            VertxOptions options = new VertxOptions();
            vertx = Vertx.vertx(options);
            started = true;
        }
    }

    public void stop() {
        if (!stoped) {
            clientTables.values().parallelStream().forEach(NetClient::close);
            stoped = true;
        }
    }

    public CompletableFuture<Void> sendReply(short replyType, Object replyData, String replyAddress) {
        return CompletableFuture.runAsync(() -> {
            SendReplyContext context = new SendReplyContext(replyType, replyData, replyAddress);
            try {
                RemoteReply remotingReply = new RemoteReply();
                remotingReply.setCode(context.getReplyType());
                if (context.getReplyType() == CommandReturnType.CommandExecuted.getValue()) {
                    remotingReply.setCommandResult((CommandResult) context.getReplyData());
                } else if (context.getReplyType() == CommandReturnType.EventHandled.getValue()) {
                    remotingReply.setEventHandledMessage((DomainEventHandledMessage) context.getReplyData());
                }
                String message = JsonTool.serialize(remotingReply);
                InetSocketAddress inetSocketAddress = RemotingUtil.string2SocketAddress(replyAddress);
                SocketAddress address = SocketAddress.inetSocketAddress(inetSocketAddress.getPort(), inetSocketAddress.getHostName());
                clientTables.putIfAbsent(replyAddress, vertx.createNetClient().connect(address, res -> {
                    if (res.succeeded()) {
                        NetSocket socket = res.result();
                        socket.endHandler(v -> socket.close()).exceptionHandler(t -> {
                            logger.error("Socket error", t);
                            socket.close();
                        }).handler(buffer -> {
                            String greeting = buffer.toString("UTF-8");
                            logger.info("Net client receiving: {}", greeting);
                        }).closeHandler(v -> clientTables.remove(replyAddress));
                        socket.write(message);
                    } else {
                        logger.error("Failed to connect Net server", res.cause());
                        clientTables.remove(replyAddress);
                    }
                }));
            } catch (Exception ex) {
                logger.error("Send command reply has exception, replyAddress: {}", context.getReplyAddress(), ex);
            }
        });
    }

    class SendReplyContext {
        private short replyType;
        private Object replyData;
        private String replyAddress;

        public SendReplyContext(short replyType, Object replyData, String replyAddress) {
            this.replyType = replyType;
            this.replyData = replyData;
            this.replyAddress = replyAddress;
        }

        public short getReplyType() {
            return replyType;
        }

        public Object getReplyData() {
            return replyData;
        }

        public String getReplyAddress() {
            return replyAddress;
        }
    }
}

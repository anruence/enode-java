package org.enodeframework.queue;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.common.SysProperties;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Address;
import org.enodeframework.common.utilities.RemoteReply;
import org.enodeframework.common.utilities.RemotingUtil;
import org.enodeframework.queue.domainevent.DomainEventHandledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author anruence@gmail.com
 */
public class SendReplyService {
    private static final Logger logger = LoggerFactory.getLogger(SendReplyService.class);
    private final ConcurrentHashMap<String, CompletableFuture<NetSocket>> socketMap = new ConcurrentHashMap<>();
    private boolean started;
    private boolean stoped;
    private NetClient netClient;

    public void start() {
        if (!started) {
            VertxOptions options = new VertxOptions();
            netClient = Vertx.vertx(options).createNetClient(new NetClientOptions());
            started = true;
        }
    }

    public void stop() {
        if (!stoped) {
            netClient.close();
            stoped = true;
        }
    }

    public CompletableFuture<Void> sendReply(short replyType, Object replyData, String replyAddress) {
        SendReplyContext context = new SendReplyContext(replyType, replyData, replyAddress);
        RemoteReply remoteReply = new RemoteReply();
        remoteReply.setCode(context.getReplyType());
        if (context.getReplyType() == CommandReturnType.CommandExecuted.getValue()) {
            remoteReply.setCommandResult((CommandResult) context.getReplyData());
        } else if (context.getReplyType() == CommandReturnType.EventHandled.getValue()) {
            remoteReply.setEventHandledMessage((DomainEventHandledMessage) context.getReplyData());
        }
        String message = JsonTool.serialize(remoteReply) + SysProperties.DELIMITED;
        Address address = RemotingUtil.string2Address(replyAddress);
        SocketAddress socketAddress = SocketAddress.inetSocketAddress(address.getPort(), address.getHost());
        CompletableFuture<NetSocket> future = new CompletableFuture<>();
        if (socketMap.putIfAbsent(replyAddress, future) == null) {
            netClient.connect(socketAddress, res -> {
                if (!res.succeeded()) {
                    future.completeExceptionally(res.cause());
                    logger.error("Failed to connect NetServer", res.cause());
                    return;
                }
                NetSocket socket = res.result();
                socket.endHandler(v -> socket.close()).exceptionHandler(t -> {
                    socketMap.remove(replyAddress);
                    logger.error("NetSocket occurs unexpected error", t);
                    socket.close();
                }).handler(buffer -> {
                    String greeting = buffer.toString("UTF-8");
                    logger.info("NetClient receiving: {}", greeting);
                }).closeHandler(v -> {
                    socketMap.remove(replyAddress);
                    logger.info("NetClient socket closed: {}", replyAddress);
                });
                future.complete(socket);
            });
        }
        return socketMap.get(replyAddress).thenAccept(socket -> {
            socket.write(message);
        }).exceptionally(ex -> {
            logger.error("Send command reply has exception, replyAddress: {}", context.getReplyAddress(), ex);
            return null;
        });
    }

    static class SendReplyContext {
        private final short replyType;
        private final Object replyData;
        private final String replyAddress;

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

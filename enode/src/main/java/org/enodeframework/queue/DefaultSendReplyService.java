package org.enodeframework.queue;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.common.SysProperties;
import org.enodeframework.common.io.ReplySocketAddress;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.common.utilities.InetUtil;
import org.enodeframework.common.utilities.ReplyMessage;
import org.enodeframework.queue.domainevent.DomainEventHandledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author anruence@gmail.com
 * TODO 支持其他类型的服务间调用
 */
public class DefaultSendReplyService extends AbstractVerticle implements ISendReplyService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSendReplyService.class);

    private final ConcurrentHashMap<String, CompletableFuture<NetSocket>> socketMap = new ConcurrentHashMap<>();
    private final ISerializeService serializeService;
    private boolean started;
    private boolean stoped;
    private NetClient netClient;

    public DefaultSendReplyService(ISerializeService serializeService) {
        this.serializeService = serializeService;
    }

    @Override
    public void start() {
        if (!started) {
            netClient = vertx.createNetClient(new NetClientOptions());
            started = true;
        }
    }

    @Override
    public void stop() {
        if (!stoped) {
            netClient.close();
            stoped = true;
        }
    }

    @Override
    public CompletableFuture<Void> sendCommandReply(CommandResult commandResult, ReplySocketAddress replyAddress) {
        ReplyMessage replyMessage = new ReplyMessage();
        replyMessage.setCode(CommandReturnType.CommandExecuted.getValue());
        replyMessage.setCommandResult(commandResult);
        return sendReply(replyMessage, replyAddress);
    }

    @Override
    public CompletableFuture<Void> sendEventReply(DomainEventHandledMessage eventHandledMessage, ReplySocketAddress replyAddress) {
        ReplyMessage replyMessage = new ReplyMessage();
        replyMessage.setCode(CommandReturnType.EventHandled.getValue());
        replyMessage.setEventHandledMessage(eventHandledMessage);
        return sendReply(replyMessage, replyAddress);
    }

    public CompletableFuture<Void> sendReply(ReplyMessage replyMessage, ReplySocketAddress replyAddress) {
        String message = serializeService.serialize(replyMessage) + SysProperties.DELIMITED;
        String key = InetUtil.toUri(replyAddress);
        SocketAddress socketAddress = SocketAddress.inetSocketAddress(replyAddress.getPort(), replyAddress.getHost());
        CompletableFuture<NetSocket> future = new CompletableFuture<>();
        if (socketMap.putIfAbsent(key, future) == null) {
            netClient.connect(socketAddress, res -> {
                if (!res.succeeded()) {
                    future.completeExceptionally(res.cause());
                    logger.error("Failed to connect NetServer, key: {}", key, res.cause());
                    return;
                }
                NetSocket socket = res.result();
                socket.endHandler(v -> socket.close()).exceptionHandler(t -> {
                    socketMap.remove(key);
                    logger.error("NetSocket occurs unexpected error", t);
                    socket.close();
                }).handler(buffer -> {
                }).closeHandler(v -> {
                    socketMap.remove(key);
                    logger.info("NetClient socket closed: {}", key);
                });
                future.complete(socket);
            });
        }
        return socketMap.get(key).thenAccept(socket -> {
            socket.write(message);
        }).exceptionally(ex -> {
            logger.error("Send command reply has exception, key: {}", key, ex);
            return null;
        });
    }
}

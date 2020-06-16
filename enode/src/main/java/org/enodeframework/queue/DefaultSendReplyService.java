package org.enodeframework.queue;

import io.vertx.core.AbstractVerticle;
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
 * TODO 支持其他类型的服务间调用
 */
public class DefaultSendReplyService extends AbstractVerticle implements ISendReplyService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSendReplyService.class);

    private final ConcurrentHashMap<String, CompletableFuture<NetSocket>> socketMap = new ConcurrentHashMap<>();

    private boolean started;

    private boolean stoped;

    private NetClient netClient;

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
    public CompletableFuture<Void> sendCommandReply(CommandResult commandResult, String replyAddress) {
        RemoteReply remoteReply = new RemoteReply();
        remoteReply.setCode(CommandReturnType.CommandExecuted.getValue());
        remoteReply.setCommandResult(commandResult);
        return sendReply(remoteReply, replyAddress);
    }

    @Override
    public CompletableFuture<Void> sendEventReply(DomainEventHandledMessage eventHandledMessage, String replyAddress) {
        RemoteReply remoteReply = new RemoteReply();
        remoteReply.setCode(CommandReturnType.EventHandled.getValue());
        remoteReply.setEventHandledMessage(eventHandledMessage);
        return sendReply(remoteReply, replyAddress);
    }

    public CompletableFuture<Void> sendReply(RemoteReply remoteReply, String replyAddress) {
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
            logger.error("Send command reply has exception, replyAddress: {}", replyAddress, ex);
            return null;
        });
    }
}

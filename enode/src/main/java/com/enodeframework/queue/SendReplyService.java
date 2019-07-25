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
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author anruence@gmail.com
 */
public class SendReplyService {
    private static final Logger logger = LoggerFactory.getLogger(SendReplyService.class);
    private boolean started;
    private boolean stoped;
    private NetClient netClient;
    private ConcurrentHashMap<String, NetSocket> socketMap = new ConcurrentHashMap<>();

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
        return CompletableFuture.runAsync(() -> {
            SendReplyContext context = new SendReplyContext(replyType, replyData, replyAddress);
            try {
                RemoteReply remoteReply = new RemoteReply();
                remoteReply.setCode(context.getReplyType());
                if (context.getReplyType() == CommandReturnType.CommandExecuted.getValue()) {
                    remoteReply.setCommandResult((CommandResult) context.getReplyData());
                } else if (context.getReplyType() == CommandReturnType.EventHandled.getValue()) {
                    remoteReply.setEventHandledMessage((DomainEventHandledMessage) context.getReplyData());
                }
                String message = JsonTool.serialize(remoteReply);
                InetSocketAddress inetSocketAddress = RemotingUtil.string2SocketAddress(replyAddress);
                SocketAddress address = SocketAddress.inetSocketAddress(inetSocketAddress.getPort(), inetSocketAddress.getHostName());
                if (!socketMap.containsKey(replyAddress)) {
                    netClient.connect(address, res -> {
                        if (!res.succeeded()) {
                            logger.error("Failed to connect NetServer", res.cause());
                            return;
                        }
                        NetSocket socket = res.result();
                        socketMap.put(replyAddress, socket);
                        socket.endHandler(v -> socket.close()).exceptionHandler(t -> {
                            logger.error("NetSocket occurs unexpected error", t);
                            socketMap.remove(replyAddress);
                            socket.close();
                        }).handler(buffer -> {
                            String greeting = buffer.toString("UTF-8");
                            logger.info("NetClient receiving: {}", greeting);
                        }).closeHandler(v -> {
                            logger.info("NetClient socket closed: {}", replyAddress);
                            socketMap.remove(replyAddress);
                        });
                        socket.write(message);
                    });
                } else {
                    socketMap.get(replyAddress).write(message);
                }
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

package com.enode.queue;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.remoting.RemotingClient;
import com.enode.common.remoting.netty.NettyClientConfig;
import com.enode.common.remoting.netty.NettyRemotingClient;
import com.enode.common.remoting.protocol.RemotingCommand;
import com.enode.common.serializing.IJsonSerializer;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class SendReplyService {
    private static final Logger _logger = ENodeLogger.getLog();

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    private RemotingClient remotingClient;
    private IJsonSerializer _jsonSerializer;
    private boolean started;
    private boolean stoped;

    @Inject
    public SendReplyService(IJsonSerializer jsonSerializer) {
        _jsonSerializer = jsonSerializer;

        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        nettyClientConfig.setClientChannelMaxIdleTimeSeconds(3600);
        remotingClient = new NettyRemotingClient(nettyClientConfig);
    }

    public void start() {
        if (!started) {
            remotingClient.start();
            started = true;
        }
    }

    public void stop() {
        if (!stoped) {
            remotingClient.shutdown();
            stoped = true;
        }
    }

    public CompletableFuture<Void> sendReply(short replyType, Object replyData, String replyAddress) {
        return CompletableFuture.runAsync(() -> {
            SendReplyContext context = new SendReplyContext(replyType, replyData, replyAddress);
            try {
                String message = _jsonSerializer.serialize(context.replyData);
                byte[] body = message.getBytes(CHARSET_UTF8);
                RemotingCommand request = RemotingCommand.createRequestCommand(context.getReplyType(), null);
                request.setBody(body);
                remotingClient.invokeOneway(replyAddress, request, 1000 * 5);
            } catch (Exception ex) {
                _logger.error("Send command reply has exception, replyAddress: " + context.getReplyAddress(), ex);
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

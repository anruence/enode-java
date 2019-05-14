package com.enode.common.remoting;

import com.enode.common.remoting.exception.RemotingSendRequestException;
import com.enode.common.remoting.exception.RemotingTimeoutException;
import com.enode.common.remoting.exception.RemotingTooMuchRequestException;
import com.enode.common.remoting.netty.NettyRequestProcessor;
import com.enode.common.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;

import java.net.SocketAddress;

public interface RemotingServer extends RemotingService {
    /**
     * 注册类型处理器
     */
    void registerProcessor(final int requestCode, final NettyRequestProcessor processor);

    void registerDefaultProcessor(final NettyRequestProcessor processor);

    int localListenPort();

    SocketAddress bindAddress();

    RemotingCommand invokeSync(final Channel channel, final RemotingCommand request, final long timeoutMillis) throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException;

    void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis, final InvokeCallback invokeCallback) throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    void invokeOneway(final Channel channel, final RemotingCommand request, final long timeoutMillis) throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;
}

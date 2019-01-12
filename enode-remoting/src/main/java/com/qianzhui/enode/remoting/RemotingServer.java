package com.qianzhui.enode.remoting;

import com.qianzhui.enode.remoting.exception.RemotingSendRequestException;
import com.qianzhui.enode.remoting.exception.RemotingTimeoutException;
import com.qianzhui.enode.remoting.exception.RemotingTooMuchRequestException;
import com.qianzhui.enode.remoting.netty.NettyRequestProcessor;
import com.qianzhui.enode.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;

public interface RemotingServer extends RemotingService {
    void registerProcessor(final int requestCode, final NettyRequestProcessor processor);

    void registerDefaultProcessor(final NettyRequestProcessor processor);

    int localListenPort();

    SocketAddress bindAddress();

    RemotingCommand invokeSync(final Channel channel, final RemotingCommand request,
                               final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
            RemotingTimeoutException;

    void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis,
                     final InvokeCallback invokeCallback) throws InterruptedException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    void invokeOneway(final Channel channel, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
            RemotingSendRequestException;
}

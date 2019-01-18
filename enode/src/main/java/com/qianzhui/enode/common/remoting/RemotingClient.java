package com.qianzhui.enode.common.remoting;

import com.qianzhui.enode.common.remoting.exception.RemotingConnectException;
import com.qianzhui.enode.common.remoting.exception.RemotingSendRequestException;
import com.qianzhui.enode.common.remoting.exception.RemotingTimeoutException;
import com.qianzhui.enode.common.remoting.exception.RemotingTooMuchRequestException;
import com.qianzhui.enode.common.remoting.netty.NettyRequestProcessor;
import com.qianzhui.enode.common.remoting.protocol.RemotingCommand;

public interface RemotingClient extends RemotingService {
    RemotingCommand invokeSync(final String addr, final RemotingCommand request,
                               final long timeoutMillis) throws InterruptedException, RemotingConnectException,
            RemotingSendRequestException, RemotingTimeoutException;

    void invokeAsync(final String addr, final RemotingCommand request, final long timeoutMillis,
                     final InvokeCallback invokeCallback) throws InterruptedException, RemotingConnectException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException;

    void registerProcessor(final int requestCode, final NettyRequestProcessor processor);

    boolean isChannelWritable(final String addr);
}

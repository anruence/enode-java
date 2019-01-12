package com.qianzhui.enode.rocketmq.client.ons;

import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;

import static com.qianzhui.enode.rocketmq.client.ons.SessionCredentials.*;

/**
 * @auther lansheng.zj
 */
public class ClientRPCHook extends AbstractRPCHook {

    private SessionCredentials sessionCredentials;


    public ClientRPCHook(SessionCredentials sessionCredentials) {
        this.sessionCredentials = sessionCredentials;
    }


    @Override
    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {
        byte[] total = AuthUtil.combineRequestContent(request,
                parseRequestContent(request, sessionCredentials.getAccessKey(), sessionCredentials.getOnsChannel().name()));
        String signature = AuthUtil.calSignature(total, sessionCredentials.getSecretKey());
        request.addExtField(Signature, signature);
        request.addExtField(AccessKey, sessionCredentials.getAccessKey());
        request.addExtField(ONSChannelKey, sessionCredentials.getOnsChannel().name());
    }


    @Override
    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {

    }

}

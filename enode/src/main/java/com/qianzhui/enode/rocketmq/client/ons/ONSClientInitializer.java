package com.qianzhui.enode.rocketmq.client.ons;

import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.namesrv.TopAddressing;
import com.qianzhui.enode.rocketmq.client.MQClientInitializer;
import com.qianzhui.enode.rocketmq.client.RocketMQClientException;

import java.util.Properties;

public class ONSClientInitializer extends MQClientInitializer {
    protected final SessionCredentials sessionCredentials = new SessionCredentials();

    // 内网地址服务器
    protected static final String WSADDR_INTERNAL = System.getProperty(
            "com.aliyun.openservices.ons.addr.internal",
            "http://onsaddr-internal.aliyun.com:8080/rocketmq/nsaddr4client-internal");

    // 公网地址服务器
    protected static final String WSADDR_INTERNET = System.getProperty(
            "com.aliyun.openservices.ons.addr.internet",
            "http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet");

    protected static final long WSADDR_INTERNAL_TIMEOUTMILLS = Long.parseLong(System.getProperty(
            "com.aliyun.openservices.ons.addr.internal.timeoutmills", "3000"));

    protected static final long WSADDR_INTERNET_TIMEOUTMILLS = Long.parseLong(System.getProperty(
            "com.aliyun.openservices.ons.addr.internet.timeoutmills", "5000"));

    public ONSClientInitializer() {

    }

    @Override
    protected void init(Properties properties){
        this.sessionCredentials.updateContent(properties);
        // 检测必须的参数
        if (null == this.sessionCredentials.getAccessKey()
                || "".equals(this.sessionCredentials.getAccessKey())) {
            throw new RocketMQClientException("please set access key");
        }

        if (null == this.sessionCredentials.getSecretKey()
                || "".equals(this.sessionCredentials.getSecretKey())) {
            throw new RocketMQClientException("please set secret key");
        }

        super.init(properties);
    }

    @Override
    public String buildIntanceName() {
        return Integer.toString(UtilAll.getPid())//
                + "#" + this.nameServerAddr.hashCode() //
                + "#" + this.sessionCredentials.getAccessKey().hashCode() + "#" + System.nanoTime();
    }

    @Override
    protected String fetchNameServerAddr() {
        // 用户指定了地址服务器
        String property = this.properties.getProperty(PropertyKeyConst.ONSAddr);
        if (property != null) {
            TopAddressing top = new TopAddressing(property);
            return top.fetchNSAddr();
        }

        // 用户未指定，默认访问内网地址服务器
        {
            TopAddressing top = new TopAddressing(WSADDR_INTERNAL);
            String nsAddrs = top.fetchNSAddr(false, WSADDR_INTERNAL_TIMEOUTMILLS);
            if (nsAddrs != null) {
                return nsAddrs;
            }
        }

        // 用户未指定，然后访问公网地址服务器
        {
            TopAddressing top = new TopAddressing(WSADDR_INTERNET);
            String nsAddrs = top.fetchNSAddr(false, WSADDR_INTERNET_TIMEOUTMILLS);
            if (nsAddrs != null) {
                return nsAddrs;
            }
        }

        return null;
    }
}

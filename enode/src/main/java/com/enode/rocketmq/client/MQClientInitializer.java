package com.enode.rocketmq.client;

import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.UtilAll;
import com.enode.rocketmq.client.ons.FAQ;
import com.enode.rocketmq.client.ons.PropertyKeyConst;

import java.util.Properties;

public class MQClientInitializer {

    protected Properties properties;
    protected String nameServerAddr = System.getProperty(MixAll.NAMESRV_ADDR_PROPERTY,
            System.getenv(MixAll.NAMESRV_ADDR_ENV));

    public MQClientInitializer() {
    }

    protected void init(Properties properties) {
        this.properties = properties;

        // 用户指定了Name Server
        String property = this.properties.getProperty(PropertyKeyConst.NAMESRV_ADDR);
        if (property != null) {
            this.nameServerAddr = property;
            return;
        }

        /**
         * 优先级 1、Name Server设置优先级最高 2、其次是地址服务器
         */
        if (null == this.nameServerAddr) {
            String addr = this.fetchNameServerAddr();
            if (null != addr) {
                this.nameServerAddr = addr;
            }
        }

        if (null == this.nameServerAddr) {
            throw new RocketMQClientException(FAQ.errorMessage(
                    "Can not find name server, May be your network problem.", FAQ.FIND_NS_FAILED));
        }
    }

    public String buildIntanceName() {
        return Integer.toString(UtilAll.getPid())//
                + "#" + this.nameServerAddr.hashCode();
    }

    public String getNameServerAddr() {
        return this.nameServerAddr;
    }

    protected String fetchNameServerAddr() {
        return null;
    }
}

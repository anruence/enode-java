package com.qianzhui.enode.rocketmq.client.ons;

import com.alibaba.rocketmq.remoting.CommandCustomHeader;
import com.alibaba.rocketmq.remoting.RPCHook;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;

import java.lang.reflect.Field;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.qianzhui.enode.rocketmq.client.ons.SessionCredentials.AccessKey;
import static com.qianzhui.enode.rocketmq.client.ons.SessionCredentials.ONSChannelKey;

public abstract class AbstractRPCHook implements RPCHook {
    protected ConcurrentHashMap<Class<? extends CommandCustomHeader>, Field[]> fieldCache =
            new ConcurrentHashMap<Class<? extends CommandCustomHeader>, Field[]>();


    protected SortedMap<String, String> parseRequestContent(RemotingCommand request, String ak, String onsChannel) {
        CommandCustomHeader header = request.readCustomHeader();
        // sort property
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put(AccessKey, ak);
        map.put(ONSChannelKey, onsChannel);
        try {
            // add header properties
            if (null != header) {
                Field[] fields = fieldCache.get(header.getClass());
                if (null == fields) {
                    fields = header.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                    }
                    Field[] tmp = fieldCache.putIfAbsent(header.getClass(), fields);
                    if (null != tmp) {
                        fields = tmp;
                    }
                }

                for (Field field : fields) {
                    Object value = field.get(header);
                    if (null != value) {
                        map.put(field.getName(), value.toString());
                    }
                }
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException("incompatible exception.", e);
        }
    }

}

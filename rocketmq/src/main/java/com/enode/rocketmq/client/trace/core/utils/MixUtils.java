package com.enode.rocketmq.client.trace.core.utils;

import com.alibaba.rocketmq.remoting.protocol.RemotingSerializable;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class MixUtils {
    public MixUtils() {
    }

    public static String getLocalAddress() {
        try {
            try {
                Enumeration e = NetworkInterface.getNetworkInterfaces();
                ArrayList ipv4Result = new ArrayList();
                ArrayList ipv6Result = new ArrayList();

                while (e.hasMoreElements()) {
                    NetworkInterface localHost = (NetworkInterface) e.nextElement();
                    Enumeration ip = localHost.getInetAddresses();

                    while (ip.hasMoreElements()) {
                        InetAddress address = (InetAddress) ip.nextElement();
                        if (!address.isLoopbackAddress()) {
                            if (address instanceof Inet6Address) {
                                ipv6Result.add(normalizeHostAddress(address));
                            } else {
                                ipv4Result.add(normalizeHostAddress(address));
                            }
                        }
                    }
                }

                String ip1;
                String localHost2;
                if (!ipv4Result.isEmpty()) {
                    Iterator localHost3 = ipv4Result.iterator();

                    do {
                        if (!localHost3.hasNext()) {
                            localHost2 = (String) ipv4Result.get(ipv4Result.size() - 1);
                            return localHost2;
                        }

                        ip1 = (String) localHost3.next();
                    } while (ip1.startsWith("127.0") || ip1.startsWith("192.168"));

                    return ip1;
                }

                if (!ipv6Result.isEmpty()) {
                    localHost2 = (String) ipv6Result.get(0);
                    return localHost2;
                }

                InetAddress localHost1 = InetAddress.getLocalHost();
                ip1 = normalizeHostAddress(localHost1);
                return ip1;
            } catch (SocketException var10) {
                var10.printStackTrace();
            } catch (UnknownHostException var11) {
                var11.printStackTrace();
            }

            return null;
        } finally {
            ;
        }
    }

    public static String normalizeHostAddress(InetAddress localHost) {
        return localHost instanceof Inet6Address ? "[" + localHost.getHostAddress() + "]" : localHost.getHostAddress();
    }

    public static String toJson(Object obj, boolean prettyFormat) {
        return RemotingSerializable.toJson(obj, prettyFormat);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return RemotingSerializable.fromJson(json, classOfT);
    }

    public static String replaceNull(String ori) {
        return ori == null ? "" : ori;
    }
}
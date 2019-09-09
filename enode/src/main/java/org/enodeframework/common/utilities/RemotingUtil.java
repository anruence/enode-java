/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.enodeframework.common.utilities;

import org.enodeframework.common.exception.ENodeRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author anruence@gmail.com
 */
public class RemotingUtil {
    public static final String OS_NAME = System.getProperty("os.name");
    private static final Logger log = LoggerFactory.getLogger(RemotingUtil.class);
    private static boolean isLinuxPlatform = false;
    private static boolean isWindowsPlatform = false;

    static {
        if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
            isLinuxPlatform = true;
        }
        if (OS_NAME != null && OS_NAME.toLowerCase().contains("windows")) {
            isWindowsPlatform = true;
        }
    }

    public static boolean isLinuxPlatform() {
        return isLinuxPlatform;
    }

    public static Address string2Address(final String addr) {
        String[] s = addr.split(":");
        return new Address(s[0], Integer.valueOf(s[1]));
    }

    public static String parseAddress(InetSocketAddress socketAddress) {
        int port = socketAddress.getPort();
        InetAddress localAddress = socketAddress.getAddress();
        if (!isSiteLocalAddress(localAddress)) {
            try {
                localAddress = Inet4Address.getLocalHost();
            } catch (UnknownHostException e) {
                throw new ENodeRuntimeException("No local address found", e);
            }
        }
        return String.format("%s:%d", localAddress.getHostAddress(), port);
    }

    public static boolean isSiteLocalAddress(InetAddress address) {
        return address.isSiteLocalAddress() && !address.isLoopbackAddress() && !address.getHostAddress().contains(":");
    }
}

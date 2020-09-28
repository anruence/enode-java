package org.enodeframework.common.utilities;

import com.google.common.base.Strings;
import org.enodeframework.common.io.ReplySocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author anruence@gmail.com
 */
public class InetUtil {

    private final static Logger logger = LoggerFactory.getLogger(InetUtil.class);

    public static ReplySocketAddress toSocketAddress(String str) {
        try {
            if (Strings.isNullOrEmpty(str)) {
                return null;
            }
            URI uri = new URI(str);
            return new ReplySocketAddress(uri.getHost(), uri.getPort());
        } catch (URISyntaxException e) {
            logger.error("toSocketAddress error. uri: {}", str, e);
            return null;
        }
    }

    public static ReplySocketAddress toSocketAddress(InetSocketAddress address) {
        if (Objects.isNull(address)) {
            return null;
        }
        return new ReplySocketAddress(address.getAddress().getHostAddress(), address.getPort());
    }

    public static String toUri(ReplySocketAddress socketAddress) {
        if (Objects.isNull(socketAddress)) {
            return "";
        }
        return String.format("enode://%s:%d", socketAddress.getHost(), socketAddress.getPort());
    }
}

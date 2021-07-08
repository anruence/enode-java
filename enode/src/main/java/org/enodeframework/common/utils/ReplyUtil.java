package org.enodeframework.common.utils;

import com.google.common.base.Strings;
import org.enodeframework.common.remoting.ReplySocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * @author anruence@gmail.com
 */
public class ReplyUtil {

    private final static Logger logger = LoggerFactory.getLogger(ReplyUtil.class);

    public static Optional<ReplySocketAddress> toSocketAddress(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }
        try {
            URI uri = new URI(value);
            return Optional.of(new ReplySocketAddress(uri.getHost(), uri.getPort()));
        } catch (URISyntaxException e) {
            logger.error("toSocketAddress error. uri: {}", value, e);
        }
        return Optional.empty();
    }

    public static Optional<ReplySocketAddress> toSocketAddress(InetSocketAddress address) {
        if (Objects.isNull(address)) {
            return Optional.empty();
        }
        return Optional.of(new ReplySocketAddress(address.getAddress().getHostAddress(), address.getPort()));
    }

    public static String toUri(InetSocketAddress socketAddress) {
        if (Objects.isNull(socketAddress)) {
            return "";
        }
        return String.format("enode://%s:%d", socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
    }

    public static String toUri(ReplySocketAddress socketAddress) {
        if (Objects.isNull(socketAddress)) {
            return "";
        }
        return String.format("enode://%s:%d", socketAddress.getHost(), socketAddress.getPort());
    }
}

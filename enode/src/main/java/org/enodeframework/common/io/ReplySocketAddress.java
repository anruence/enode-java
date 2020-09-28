package org.enodeframework.common.io;

import java.io.Serializable;

public class ReplySocketAddress implements Serializable {

    private String host;

    private int port;

    public ReplySocketAddress() {

    }

    public ReplySocketAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
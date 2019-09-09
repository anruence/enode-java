package org.enodeframework.common.utilities;

public class Address {
    private String host;

    private int port;

    public Address(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public Address setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public Address setPort(int port) {
        this.port = port;
        return this;
    }
}

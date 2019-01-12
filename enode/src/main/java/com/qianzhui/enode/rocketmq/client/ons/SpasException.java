package com.qianzhui.enode.rocketmq.client.ons;

/**
 * copy from spas
 *
 * @auther lansheng.zj
 */
public class SpasException extends RuntimeException {
    private static final long serialVersionUID = 1L;


    public SpasException(String message, Throwable t) {
        super(message, t);
    }


    public SpasException(String message) {
        super(message);
    }
}
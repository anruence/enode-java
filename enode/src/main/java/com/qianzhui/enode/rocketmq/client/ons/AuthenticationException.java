package com.qianzhui.enode.rocketmq.client.ons;

public class AuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String status;
    private int code;


    public AuthenticationException(String status, int code) {
        super();
        this.status = status;
        this.code = code;
    }


    public AuthenticationException(String status, int code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }


    public AuthenticationException(String status, int code, Throwable throwable) {
        super(throwable);
        this.status = status;
        this.code = code;
    }


    public AuthenticationException(String status, int code, String message, Throwable throwable) {
        super(message, throwable);
        this.status = status;
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

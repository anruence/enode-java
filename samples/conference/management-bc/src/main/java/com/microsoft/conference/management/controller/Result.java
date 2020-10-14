package com.microsoft.conference.management.controller;

import java.io.Serializable;

public class Result<T> implements Serializable {
    //true ok ,false fail
    private Boolean success;
    private String errCode;
    private String errMsg;
    private T data;

    public Result() {
    }

    public Result(T data) {
        this.data = data;
        this.success = true;
    }

    public Result(String errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.success = false;
    }

    public static Result error(String errCode, String errMsg) {
        return new Result(errCode, errMsg);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result [success=" + success + ", errCode=" + errCode + ", errMsg=" + errMsg + ", data=" + data + "]";
    }
}
package com.microsoft.conference.management.controller;

public class ActionResult<T> {
    private boolean success;
    private String errCode;
    private String errMsg;
    private T data;

    public ActionResult(T data) {
        this.data = data;
        this.success = true;
    }

    public ActionResult(String errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.success = false;
    }

    public ActionResult<T> of(T data) {
        return new ActionResult<>(data);
    }

    public static ActionResult error(String errCode, String errMsg) {
        return new ActionResult<>(errCode, errMsg);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
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
}

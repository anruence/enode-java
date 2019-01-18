package com.enode.common.io;

public class AsyncTaskResult<T> {

    public static AsyncTaskResult Success = new AsyncTaskResult(AsyncTaskStatus.Success, null);

    private AsyncTaskStatus status;

    private String errorMessage;

    private T data;

    public AsyncTaskResult(AsyncTaskStatus status) {
        this(status, null, null);
    }

    public AsyncTaskResult(AsyncTaskStatus status, String errorMessage) {
        this(status, errorMessage, null);
    }

    public AsyncTaskResult(AsyncTaskStatus status, T data) {
        this(status, null, data);
    }

    public AsyncTaskResult(AsyncTaskStatus status, String errorMessage, T data) {
        this.status = status;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public static AsyncTaskResult getSuccess() {
        return Success;
    }

    public AsyncTaskStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T getData() {
        return data;
    }
}

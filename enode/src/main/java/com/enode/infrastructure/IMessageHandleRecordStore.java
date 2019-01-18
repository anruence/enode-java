package com.enode.infrastructure;

import com.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandleRecordStore {
    CompletableFuture<AsyncTaskResult> addRecordAsync(MessageHandleRecord record);

    CompletableFuture<AsyncTaskResult> addRecordAsync(TwoMessageHandleRecord record);

    CompletableFuture<AsyncTaskResult> addRecordAsync(ThreeMessageHandleRecord record);

    CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId, String handlerTypeName, String aggregateRootTypeName);

    CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId1, String messageId2, String handlerTypeName, String aggregateRootTypeName);

    CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId1, String messageId2, String messageId3, String handlerTypeName, String aggregateRootTypeName);
}

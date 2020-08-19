package org.enodeframework.mongo;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.enodeframework.common.exception.EventStoreException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.AggregateEventAppendResult;
import org.enodeframework.eventing.BatchAggregateEventAppendResult;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventAppendResult;
import org.enodeframework.eventing.EventAppendStatus;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.eventing.IEventStore;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class MongoEventStore implements IEventStore {
    
    private static final Logger logger = LoggerFactory.getLogger(MongoEventStore.class);

    private static final Pattern PATTERN = Pattern.compile("\\{.+?commandId: \"(.+?)\" }$");

    private final int duplicateCode;

    private final String versionIndexName;

    private final String commandIndexName;

    private final MongoClient mongoClient;

    private final MongoConfiguration mongoConfiguration;

    private final IEventSerializer eventSerializer;

    private final ISerializeService serializeService;

    public MongoEventStore(MongoClient mongoClient, IEventSerializer eventSerializer, ISerializeService serializeService) {
        this(mongoClient, new MongoConfiguration(), eventSerializer, serializeService);
    }

    public MongoEventStore(MongoClient mongoClient, MongoConfiguration mongoConfiguration, IEventSerializer eventSerializer, ISerializeService serializeService) {
        this.mongoClient = mongoClient;
        this.eventSerializer = eventSerializer;
        this.mongoConfiguration = mongoConfiguration;
        this.duplicateCode = mongoConfiguration.getDuplicateCode();
        this.versionIndexName = mongoConfiguration.getEventTableVersionUniqueIndexName();
        this.commandIndexName = mongoConfiguration.getEventTableCommandIdUniqueIndexName();
        this.serializeService = serializeService;
    }

    @Override
    public CompletableFuture<EventAppendResult> batchAppendAsync(List<DomainEventStream> eventStreams) {
        CompletableFuture<EventAppendResult> future = new CompletableFuture<>();
        EventAppendResult appendResult = new EventAppendResult();
        if (eventStreams.size() == 0) {
            future.complete(appendResult);
            return future;
        }
        Map<String, List<DomainEventStream>> eventStreamMap = eventStreams.stream().distinct().collect(Collectors.groupingBy(DomainEventStream::getAggregateRootId));
        BatchAggregateEventAppendResult batchAggregateEventAppendResult = new BatchAggregateEventAppendResult(eventStreamMap.keySet().size());
        for (Map.Entry<String, List<DomainEventStream>> entry : eventStreamMap.entrySet()) {
            batchAppendAggregateEventsAsync(entry.getKey(), entry.getValue(), batchAggregateEventAppendResult, 0);
        }
        return batchAggregateEventAppendResult.taskCompletionSource;
    }

    private void batchAppendAggregateEventsAsync(String aggregateRootId, List<DomainEventStream> eventStreamList, BatchAggregateEventAppendResult batchAggregateEventAppendResult, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("BatchAppendAggregateEventsAsync",
                () -> batchAppendAggregateEventsAsync(aggregateRootId, eventStreamList),
                result -> {
                    batchAggregateEventAppendResult.addCompleteAggregate(aggregateRootId, result);
                },
                () -> String.format("[aggregateRootId: %s, eventStreamCount: %s]", aggregateRootId, eventStreamList.size()),
                null,
                retryTimes, true);
    }

    private CompletableFuture<DomainEventStream> tryFindEventByCommandIdAsync(String aggregateRootId, String commandId, List<String> duplicateCommandIds, int retryTimes) {
        CompletableFuture<DomainEventStream> future = new CompletableFuture<>();
        IOHelper.tryAsyncActionRecursively("TryFindEventByCommandIdAsync",
                () -> findAsync(aggregateRootId, commandId),
                result -> {
                    if (result != null) {
                        duplicateCommandIds.add(result.getCommandId());
                    }
                    future.complete(result);
                },
                () -> String.format("[aggregateRootId:%s, commandId:%s]", aggregateRootId, commandId),
                null,
                retryTimes, true);
        return future;
    }

    private CompletableFuture<AggregateEventAppendResult> batchAppendAggregateEventsAsync(String aggregateRootId, List<DomainEventStream> eventStreamList) {
        CompletableFuture<AggregateEventAppendResult> future = new CompletableFuture<>();
        List<Document> documents = Lists.newArrayList();
        for (DomainEventStream domainEventStream : eventStreamList) {
            Document document = new Document();
            document.put("aggregateRootId", domainEventStream.getAggregateRootId());
            document.put("aggregateRootTypeName", domainEventStream.getAggregateRootTypeName());
            document.put("commandId", domainEventStream.getCommandId());
            document.put("version", domainEventStream.getVersion());
            document.put("gmtCreate", domainEventStream.getTimestamp());
            document.put("events", serializeService.serialize(eventSerializer.serialize(domainEventStream.events())));
            documents.add(document);
        }
        if (documents.size() > 1) {
            future = batchInsertAsync(documents);
        } else {
            future = insertOneByOneAsync(documents);
        }
        return future.exceptionally(throwable -> {
            int code = 0;
            String message = "";
            if (throwable instanceof MongoWriteException) {
                MongoWriteException ex = (MongoWriteException) throwable;
                code = ex.getCode();
                message = ex.getMessage();
            }
            if (throwable instanceof MongoBulkWriteException) {
                MongoBulkWriteException ex = (MongoBulkWriteException) throwable;
                if (ex.getWriteErrors().size() >= 1) {
                    BulkWriteError writeError = ex.getWriteErrors().get(0);
                    code = writeError.getCode();
                    message = writeError.getMessage();
                }
            }
            if (code == duplicateCode && message.contains(versionIndexName)) {
                AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                appendResult.setEventAppendStatus(EventAppendStatus.DuplicateEvent);
                return appendResult;
            }
            if (code == duplicateCode && message.contains(commandIndexName)) {
                // E11000 duplicate key error collection: enode.event_stream index: aggregateRootId_1_commandId_1 dup key: { aggregateRootId: "5ee8b610d7671114741829c7", commandId: "5ee8b61bd7671114741829cf" }
                AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                appendResult.setEventAppendStatus(EventAppendStatus.DuplicateCommand);
                String commandId = parseDuplicateCommandId(message);
                if (!Strings.isNullOrEmpty(commandId)) {
                    appendResult.setDuplicateCommandIds(Lists.newArrayList(commandId));
                    return appendResult;
                }
                return appendResult;
            }
            logger.error("Batch append event has unknown exception.", throwable);
            throw new EventStoreException(throwable);
        });
    }

    public CompletableFuture<AggregateEventAppendResult> batchInsertAsync(List<Document> documents) {
        CompletableFuture<AggregateEventAppendResult> future = new CompletableFuture<>();
        mongoClient.getDatabase(mongoConfiguration.getDatabaseName()).getCollection(mongoConfiguration.getEventCollectionName())
                .insertMany(documents).subscribe(new Subscriber<InsertManyResult>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(InsertManyResult insertManyResult) {
                AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                appendResult.setEventAppendStatus(EventAppendStatus.Success);
                future.complete(appendResult);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                appendResult.setEventAppendStatus(EventAppendStatus.Success);
                future.complete(appendResult);
            }
        });
        return future;
    }

    public CompletableFuture<AggregateEventAppendResult> insertOneByOneAsync(List<Document> documents) {
        CompletableFuture<AggregateEventAppendResult> future = new CompletableFuture<>();
        CountDownLatch latch = new CountDownLatch(documents.size());
        for (Document document : documents) {
            mongoClient.getDatabase(mongoConfiguration.getDatabaseName()).getCollection(mongoConfiguration.getEventCollectionName())
                    .insertOne(document).subscribe(new Subscriber<InsertOneResult>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(InsertOneResult insertOneResult) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable t) {
                    latch.countDown();
                    future.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    if (latch.getCount() == 0) {
                        AggregateEventAppendResult appendResult = new AggregateEventAppendResult();
                        appendResult.setEventAppendStatus(EventAppendStatus.Success);
                        future.complete(appendResult);
                    }
                }
            });
            if (future.isDone()) {
                break;
            }
        }
        return future;
    }

    private String parseDuplicateCommandId(String errMsg) {
        Matcher matcher = PATTERN.matcher(errMsg);
        if (matcher.find()) {
            if (matcher.groupCount() == 1) {
                return matcher.group(1);
            }
        }
        return "";
    }

    @Override
    public CompletableFuture<List<DomainEventStream>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion) {
        return IOHelper.tryIOFuncAsync(() -> {
            CompletableFuture<List<DomainEventStream>> future = new CompletableFuture<>();

            Bson filter = Filters.and(Filters.eq("aggregateRootId", aggregateRootId),
                    Filters.gte("version", minVersion),
                    Filters.lte("version", maxVersion));
            Bson sort = Sorts.ascending("version");
            mongoClient.getDatabase(mongoConfiguration.getDatabaseName()).getCollection(mongoConfiguration.getEventCollectionName())
                    .find(filter).sort(sort).subscribe(new Subscriber<Document>() {
                final List<DomainEventStream> streams = Lists.newArrayList();

                @Override
                public void onSubscribe(Subscription s) {
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Document document) {
                    DomainEventStream eventStream = new DomainEventStream(
                            document.getString("commandId"),
                            document.getString("aggregateRootId"),
                            document.getString("aggregateRootTypeName"),
                            document.get("gmtCreate", Date.class),
                            eventSerializer.deserialize(serializeService.deserialize(document.getString("events"), Map.class)),
                            Maps.newHashMap());
                    streams.add(eventStream);
                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    streams.sort(Comparator.comparingInt(DomainEventStream::getVersion));
                    future.complete(streams);
                }
            });
            return future.exceptionally(throwable -> {
                if (throwable instanceof MongoWriteException) {
                    MongoWriteException ex = (MongoWriteException) throwable;
                    String errorMessage = String.format("Failed to query aggregate events async, aggregateRootId: %s, aggregateRootType: %s", aggregateRootId, aggregateRootTypeName);
                    logger.error(errorMessage, ex);
                    throw new IORuntimeException(throwable);
                }
                logger.error("Failed to query aggregate events async, aggregateRootId: {}, aggregateRootType: {}", aggregateRootId, aggregateRootTypeName, throwable);
                throw new EventStoreException(throwable);
            });
        }, "QueryAggregateEventsAsync");
    }

    @Override
    public CompletableFuture<DomainEventStream> findAsync(String aggregateRootId, int version) {
        return IOHelper.tryIOFuncAsync(() -> {
            CompletableFuture<DomainEventStream> future = new CompletableFuture<>();
            Bson filter = Filters.and(Filters.eq("aggregateRootId", aggregateRootId), Filters.eq("version", version));
            mongoClient.getDatabase(mongoConfiguration.getDatabaseName()).getCollection(mongoConfiguration.getEventCollectionName())
                    .find(filter).subscribe(new Subscriber<Document>() {
                private DomainEventStream eventStream;

                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Document document) {
                    DomainEventStream eventStream = new DomainEventStream(
                            document.getString("commandId"),
                            document.getString("aggregateRootId"),
                            document.getString("aggregateRootTypeName"),
                            document.get("gmtCreate", Date.class),
                            eventSerializer.deserialize(serializeService.deserialize(document.getString("events"), Map.class)),
                            Maps.newHashMap());
                    this.eventStream = eventStream;
                    future.complete(eventStream);
                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    future.complete(eventStream);
                }
            });

            return future.exceptionally(throwable -> {
                if (throwable instanceof MongoWriteException) {
                    MongoWriteException ex = (MongoWriteException) throwable;
                    logger.error("Find event by version has sql exception, aggregateRootId: {}, version: {}", aggregateRootId, version, ex);
                    throw new IORuntimeException(throwable);
                }
                logger.error("Find event by version has unknown exception, aggregateRootId: {}, version: {}", aggregateRootId, version, throwable);
                throw new EventStoreException(throwable);
            });
        }, "FindEventByVersionAsync");

    }

    @Override
    public CompletableFuture<DomainEventStream> findAsync(String aggregateRootId, String commandId) {
        return IOHelper.tryIOFuncAsync(() -> {
            CompletableFuture<DomainEventStream> future = new CompletableFuture<>();
            Bson filter = Filters.and(Filters.eq("aggregateRootId", aggregateRootId), Filters.eq("commandId", commandId));
            mongoClient.getDatabase(mongoConfiguration.getDatabaseName()).getCollection(mongoConfiguration.getEventCollectionName())
                    .find(filter).subscribe(new Subscriber<Document>() {
                private DomainEventStream eventStream;

                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Document document) {
                    DomainEventStream eventStream = new DomainEventStream(
                            document.getString("commandId"),
                            document.getString("aggregateRootId"),
                            document.getString("aggregateRootTypeName"),
                            document.get("gmtCreate", Date.class),
                            eventSerializer.deserialize(serializeService.deserialize(document.getString("events"), Map.class)),
                            Maps.newHashMap());
                    this.eventStream = eventStream;
                    future.complete(eventStream);
                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    future.complete(eventStream);
                }
            });
            return future.exceptionally(throwable -> {
                if (throwable instanceof MongoWriteException) {
                    MongoWriteException ex = (MongoWriteException) throwable;
                    logger.error("Find event by commandId has sql exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, ex);
                    throw new IORuntimeException(throwable);
                }
                logger.error("Find event by commandId has unknown exception, aggregateRootId: {}, commandId: {}", aggregateRootId, commandId, throwable);
                throw new EventStoreException(throwable);
            });
        }, "FindEventByCommandIdAsync");
    }
}

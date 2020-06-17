package org.enodeframework.mongo;

import com.mongodb.MongoWriteException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class MongoPublishedVersionStore implements IPublishedVersionStore {
    private static final Logger logger = LoggerFactory.getLogger(MongoPublishedVersionStore.class);
    private MongoClient mongoClient;
    private int duplicateCode;
    private String uniqueIndexName;
    private MongoConfiguration configuration;

    public MongoPublishedVersionStore(MongoClient mongoClient) {
        this(mongoClient, new MongoConfiguration());
    }

    public MongoPublishedVersionStore(MongoClient mongoClient, MongoConfiguration configuration) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.uniqueIndexName = configuration.getPublishedVersionUniqueIndexName();
        this.duplicateCode = configuration.getDuplicateCode();
    }

    @Override
    public CompletableFuture<Integer> updatePublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId, int publishedVersion) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        boolean isInsert = publishedVersion == 1;
        if (isInsert) {
            Document document = new Document();
            document.put("processorName", processorName);
            document.put("aggregateRootTypeName", aggregateRootTypeName);
            document.put("aggregateRootId", aggregateRootId);
            document.put("version", 1);
            document.put("gmtCreate", new Date());
            mongoClient.getDatabase(configuration.getDatabaseName()).getCollection(configuration.getPublishedVersionCollectionName())
                    .insertOne(document).subscribe(new Subscriber<InsertOneResult>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(InsertOneResult x) {

                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    future.complete(1);
                }
            });
        } else {
            Bson filter = Filters.and(
                    Filters.eq("version", publishedVersion - 1),
                    Filters.eq("processorName", processorName),
                    Filters.eq("aggregateRootId", aggregateRootId)
            );
            Bson update = Updates.combine(
                    Updates.set("version", publishedVersion),
                    Updates.set("gmtCreate", new Date())
            );
            mongoClient.getDatabase(configuration.getDatabaseName()).getCollection(configuration.getPublishedVersionCollectionName())
                    .updateOne(filter, update).subscribe(new Subscriber<UpdateResult>() {
                private int updated;

                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(UpdateResult x) {
                    updated = ((int) x.getModifiedCount());
                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    future.complete(updated);
                }
            });
        }

        return future.exceptionally(throwable -> {
            if (throwable instanceof MongoWriteException) {
                MongoWriteException ex = (MongoWriteException) throwable;
                if (isInsert && ex.getCode() == duplicateCode && ex.getMessage().contains(uniqueIndexName)) {
                    return 0;
                }
                logger.error("Insert or update aggregate published version has sql exception.", ex);
                throw new IORuntimeException(throwable);
            }
            logger.error("Insert or update aggregate published version has unknown exception.", throwable);
            throw new ENodeRuntimeException(throwable);

        });
    }

    @Override
    public CompletableFuture<Integer> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        Bson updateFilter = Filters.and(
                Filters.eq("processorName", processorName),
                Filters.eq("aggregateRootId", aggregateRootId)
        );
        mongoClient.getDatabase(configuration.getDatabaseName()).getCollection(configuration.getPublishedVersionCollectionName()).find(updateFilter).subscribe(new Subscriber<Document>() {
            private Integer version = 0;

            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(Document document) {
                version = document.getInteger("version");
                future.complete(version);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                future.complete(version);
            }
        });
        return future.exceptionally(throwable -> {
            if (throwable instanceof SQLException) {
                SQLException ex = (SQLException) throwable;
                logger.error("Get aggregate published version has sql exception.", ex);
                throw new IORuntimeException(throwable);
            }
            logger.error("Get aggregate published version has unknown exception.", throwable);
            throw new ENodeRuntimeException(throwable);
        });
    }
}

package org.enodeframework.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.exception.PublishedVersionStoreException;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class MongoPublishedVersionStore implements IPublishedVersionStore {

    private static final Logger logger = LoggerFactory.getLogger(MongoPublishedVersionStore.class);

    private final MongoClient mongoClient;

    private final int duplicateCode;

    private final String uniqueIndexName;

    private final MongoConfiguration configuration;

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
            throw new PublishedVersionStoreException(throwable);
        });
    }
}

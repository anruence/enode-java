package org.enodeframework.mongo

import com.mongodb.MongoWriteException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoClient
import org.bson.Document
import org.enodeframework.common.exception.EventStoreException
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.eventing.IPublishedVersionStore
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class MongoPublishedVersionStore @JvmOverloads constructor(private val mongoClient: MongoClient, private val configuration: MongoConfiguration = MongoConfiguration()) : IPublishedVersionStore {
    private val duplicateCode: Int
    private val uniqueIndexName: String
    override fun updatePublishedVersionAsync(processorName: String, aggregateRootTypeName: String, aggregateRootId: String, publishedVersion: Int): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        val isInsert = publishedVersion == 1
        if (isInsert) {
            val document = Document()
            document["processorName"] = processorName
            document["aggregateRootTypeName"] = aggregateRootTypeName
            document["aggregateRootId"] = aggregateRootId
            document["version"] = 1
            document["gmtCreate"] = Date()
            mongoClient.getDatabase(configuration.databaseName).getCollection(configuration.publishedVersionCollectionName)
                    .insertOne(document).subscribe(object : Subscriber<InsertOneResult> {
                        override fun onSubscribe(s: Subscription) {
                            s.request(1)
                        }

                        override fun onNext(x: InsertOneResult) {
                            future.complete(if (x.wasAcknowledged()) 1 else 0)
                        }

                        override fun onError(t: Throwable) {
                            future.completeExceptionally(t)
                        }

                        override fun onComplete() {
                            future.complete(1)
                        }
                    })
        } else {
            val filter = Filters.and(
                    Filters.eq("version", publishedVersion - 1),
                    Filters.eq("processorName", processorName),
                    Filters.eq("aggregateRootId", aggregateRootId)
            )
            val update = Updates.combine(
                    Updates.set("version", publishedVersion),
                    Updates.set("gmtCreate", Date())
            )
            mongoClient.getDatabase(configuration.databaseName).getCollection(configuration.publishedVersionCollectionName)
                    .updateOne(filter, update).subscribe(object : Subscriber<UpdateResult> {
                        private var updated = 0
                        override fun onSubscribe(s: Subscription) {
                            s.request(1)
                        }

                        override fun onNext(x: UpdateResult) {
                            updated = x.modifiedCount.toInt()
                        }

                        override fun onError(t: Throwable) {
                            future.completeExceptionally(t)
                        }

                        override fun onComplete() {
                            future.complete(updated)
                        }
                    })
        }
        return future.exceptionally { throwable: Throwable ->
            if (throwable is MongoWriteException) {
                if (isInsert && throwable.code == duplicateCode && throwable.message!!.contains(uniqueIndexName)) {
                    return@exceptionally 0
                }
                logger.error("Insert or update aggregate published version has sql exception.", throwable)
                throw IORuntimeException(throwable)
            }
            logger.error("Insert or update aggregate published version has unknown exception.", throwable)
            throw EventStoreException(throwable)
        }
    }

    override fun getPublishedVersionAsync(processorName: String, aggregateRootTypeName: String, aggregateRootId: String): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        val updateFilter = Filters.and(
                Filters.eq("processorName", processorName),
                Filters.eq("aggregateRootId", aggregateRootId)
        )
        mongoClient.getDatabase(configuration.databaseName).getCollection(configuration.publishedVersionCollectionName).find(updateFilter).subscribe(object : Subscriber<Document> {
            private var version = 0
            override fun onSubscribe(s: Subscription) {
                s.request(1)
            }

            override fun onNext(document: Document) {
                version = document.getInteger("version")
                future.complete(version)
            }

            override fun onError(t: Throwable) {
                future.completeExceptionally(t)
            }

            override fun onComplete() {
                future.complete(version)
            }
        })
        return future.exceptionally { throwable: Throwable ->
            if (throwable is MongoWriteException) {
                logger.error("Get aggregate published version has sql exception. aggregateRootId: {}", aggregateRootId, throwable)
                throw IORuntimeException(throwable)
            }
            logger.error("Get aggregate published version has unknown exception. aggregateRootId: {}", aggregateRootId, throwable)
            throw PublishedVersionStoreException(throwable)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MongoPublishedVersionStore::class.java)
    }

    init {
        uniqueIndexName = configuration.publishedVersionUniqueIndexName
        duplicateCode = configuration.duplicateCode
    }
}
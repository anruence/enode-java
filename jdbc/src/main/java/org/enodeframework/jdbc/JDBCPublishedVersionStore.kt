package org.enodeframework.jdbc;

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.UpdateResult
import org.enodeframework.common.DbType
import org.enodeframework.common.EventStoreConfiguration
import org.enodeframework.common.exception.IORuntimeException
import org.enodeframework.common.exception.PublishedVersionStoreException
import org.enodeframework.common.io.IOHelper
import org.enodeframework.eventing.IPublishedVersionStore
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.sql.DataSource

/**
 * @author anruence@gmail.com
 */
class JDBCPublishedVersionStore(
    private val dataSource: DataSource,
    private val configuration: EventStoreConfiguration
) : AbstractVerticle(), IPublishedVersionStore {

    private lateinit var sqlClient: SQLClient

    private var code: String = ""

    override fun start() {
        super.start()
        this.sqlClient = JDBCClient.create(vertx, this.dataSource)
    }

    override fun stop() {
        super.stop()
        this.sqlClient.close()
    }

    override fun updatePublishedVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        return IOHelper.tryIOFuncAsync({
            updatePublishedVersion(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
        }, "UpdatePublishedVersionAsync");
    }

    private fun updatePublishedVersion(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        val insert = publishedVersion == 1
        if (insert) {
            return insertVersionAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
        }
        return updateVersionAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion)
    }

    private fun updateVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        val array = JsonArray()
        array.add(publishedVersion)
        array.add(Date().toInstant())
        array.add(processorName)
        array.add(aggregateRootId)
        array.add(publishedVersion - 1)
        val sql = String.format(UPDATE_SQL, configuration.publishedTableName)
        sqlClient.updateWithParams(sql, array) { ar: AsyncResult<UpdateResult> ->
            if (ar.succeeded()) {
                if (ar.result().updated == 0) {
                    future.completeExceptionally(
                        PublishedVersionStoreException(
                            String.format(
                                "version update rows is 0. %s",
                                array
                            )
                        )
                    )
                    return@updateWithParams
                }
                future.complete(ar.result().updated);
                return@updateWithParams
            }
            val throwable = ar.cause()
            if (throwable is SQLException) {
                logger.error("Update aggregate published version has sql exception. {}", array, throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@updateWithParams
            }
            logger.error("Update aggregate published version has unknown exception. {}", array, throwable)
            future.completeExceptionally(PublishedVersionStoreException(throwable))
            return@updateWithParams
        }
        return future
    }

    private fun insertVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        val array = JsonArray()
        array.add(processorName)
        array.add(aggregateRootTypeName)
        array.add(aggregateRootId)
        array.add(1)
        array.add(Date().toInstant())
        val sql = String.format(INSERT_SQL, configuration.publishedTableName)
        sqlClient.updateWithParams(sql, array) { ar: AsyncResult<UpdateResult> ->
            if (ar.succeeded()) {
                if (ar.result().updated == 0) {
                    future.completeExceptionally(
                        PublishedVersionStoreException(
                            String.format(
                                "version update rows is 0. %s",
                                array
                            )
                        )
                    )
                    return@updateWithParams
                }
                future.complete(ar.result().updated);
                return@updateWithParams
            }
            val throwable = ar.cause()
            if (throwable is SQLException) {
                if (code == throwable.sqlState && throwable.message?.contains(configuration.publishedUkName) == true) {
                    future.complete(1)
                    return@updateWithParams
                }
                logger.error("Insert aggregate published version has sql exception. {}", array, throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@updateWithParams
            }
            logger.error("Insert aggregate published version has unknown exception. {}", array, throwable)
            future.completeExceptionally(PublishedVersionStoreException(throwable))
            return@updateWithParams
        }
        return future
    }

    override fun getPublishedVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String
    ): CompletableFuture<Int> {
        return IOHelper.tryIOFuncAsync({
            getPublishedVersion(processorName, aggregateRootTypeName, aggregateRootId)
        }, "UpdatePublishedVersionAsync");
    }

    private fun getPublishedVersion(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String
    ): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        val sql = String.format(SELECT_SQL, configuration.publishedTableName)
        val array = JsonArray()
        array.add(processorName)
        array.add(aggregateRootId)
        sqlClient.querySingleWithParams(sql, array) { ar: AsyncResult<JsonArray?> ->
            if (ar.succeeded()) {
                ar.result().let { row ->
                    if (row == null) {
                        future.complete(0)
                        return@querySingleWithParams
                    }
                    future.complete(row.getInteger(0))
                    return@querySingleWithParams
                }
            }
            val throwable = ar.cause()
            if (throwable is SQLException) {
                logger.error("Get aggregate published version has sql exception.", throwable)
                future.completeExceptionally(IORuntimeException(throwable))
                return@querySingleWithParams
            }
            logger.error("Get aggregate published version has unknown exception.", throwable)
            future.completeExceptionally(PublishedVersionStoreException(throwable))
            return@querySingleWithParams
        }
        return future
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JDBCPublishedVersionStore::class.java)
        private const val INSERT_SQL =
            "INSERT INTO %s (processor_name, aggregate_root_type_name, aggregate_root_id, version, gmt_create) VALUES (?, ?, ?, ?, ?)"
        private const val UPDATE_SQL =
            "UPDATE %s SET version = ?, gmt_create = ? WHERE processor_name = ? AND aggregate_root_id = ? AND version = ?"
        private const val SELECT_SQL = "SELECT version FROM %s WHERE processor_name = ? AND aggregate_root_id = ?"
    }

    init {
        if (DbType.MySQL.name == configuration.dbType) {
            this.code = "23000"
        }
        if (DbType.Pg.name == configuration.dbType) {
            this.code = "23505"
        }
    }
}
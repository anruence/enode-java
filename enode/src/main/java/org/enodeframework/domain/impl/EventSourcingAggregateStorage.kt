package org.enodeframework.domain.impl

import org.enodeframework.common.exception.AggregateRootInvalidException
import org.enodeframework.common.exception.AggregateRootNotFoundException
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.utilities.Ensure
import org.enodeframework.domain.IAggregateRoot
import org.enodeframework.domain.IAggregateRootFactory
import org.enodeframework.domain.IAggregateSnapshotter
import org.enodeframework.domain.IAggregateStorage
import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.IEventStore
import org.enodeframework.infrastructure.ITypeNameProvider
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class EventSourcingAggregateStorage(private val eventStore: IEventStore, private val aggregateRootFactory: IAggregateRootFactory, private val aggregateSnapshotter: IAggregateSnapshotter, private val typeNameProvider: ITypeNameProvider) : IAggregateStorage {
    override fun <T : IAggregateRoot?> getAsync(aggregateRootType: Class<T>, aggregateRootId: String): CompletableFuture<T> {
        Ensure.notNull(aggregateRootId, "aggregateRootId")
        Ensure.notNull(aggregateRootType, "aggregateRootType")
        return tryGetFromSnapshot(aggregateRootId, aggregateRootType).thenCompose { aggregateRoot: T? ->
            if (aggregateRoot != null) {
                return@thenCompose CompletableFuture.completedFuture(aggregateRoot)
            }
            val aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType)
            tryQueryAggregateEventsAsync(aggregateRootType, aggregateRootTypeName, aggregateRootId, MINVERSION, MAXVERSION, 0, CompletableFuture()).thenApply { eventStreams: List<DomainEventStream>? ->
                val rebuild = rebuildAggregateRoot(aggregateRootType, eventStreams)
                        ?: throw AggregateRootNotFoundException(String.format("aggregateRoot not found, [aggregateRootType: %s, aggregateRootId: %s]", aggregateRootType.name, aggregateRootId))
                rebuild
            }
        }
    }

    private fun <T : IAggregateRoot?> tryRestoreFromSnapshotAsync(aggregateRootType: Class<T>, aggregateRootId: String, retryTimes: Int, taskSource: CompletableFuture<T>): CompletableFuture<T> {
        IOHelper.tryAsyncActionRecursively("TryRestoreFromSnapshotAsync",
                { aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId) }, { value: T -> taskSource.complete(value) },
                { String.format("tryRestoreFromSnapshotAsync has unknown exception, aggregateRootType: %s, aggregateRootId: %s", aggregateRootType.name, aggregateRootId) },
                null,
                retryTimes, true)
        return taskSource
    }

    private fun tryQueryAggregateEventsAsync(aggregateRootType: Class<*>, aggregateRootTypeName: String, aggregateRootId: String, minVersion: Int, maxVersion: Int, retryTimes: Int, taskSource: CompletableFuture<List<DomainEventStream>>): CompletableFuture<List<DomainEventStream>> {
        IOHelper.tryAsyncActionRecursively("TryQueryAggregateEventsAsync",
                { eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, minVersion, maxVersion) }, { value: List<DomainEventStream> -> taskSource.complete(value) },
                { String.format("eventStore.queryAggregateEventsAsync has unknown exception, aggregateRootTypeName: %s, aggregateRootId: %s", aggregateRootTypeName, aggregateRootId) },
                null,
                retryTimes, true)
        return taskSource
    }

    private fun <T : IAggregateRoot?> tryGetFromSnapshot(aggregateRootId: String, aggregateRootType: Class<T>): CompletableFuture<T> {
        val aggregateRootFuture = tryRestoreFromSnapshotAsync(aggregateRootType, aggregateRootId, 0, CompletableFuture())
        return aggregateRootFuture.thenCompose { aggregateRoot: T? ->
            if (aggregateRoot == null) {
                return@thenCompose CompletableFuture.completedFuture(null)
            }
            if (aggregateRoot.javaClass != aggregateRootType || aggregateRoot.uniqueId != aggregateRootId) {
                throw AggregateRootInvalidException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType: %s, aggregateRootId: %s], expected: [aggregateRootType: %s, aggregateRootId: %s]",
                        aggregateRoot.javaClass,
                        aggregateRoot.uniqueId,
                        aggregateRootType,
                        aggregateRootId))
            }
            val aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType)
            val eventStreamsFuture = tryQueryAggregateEventsAsync(aggregateRootType, aggregateRootTypeName, aggregateRootId, aggregateRoot.version + 1, MAXVERSION, 0, CompletableFuture())
            eventStreamsFuture.thenApply { eventStreams: List<DomainEventStream>? ->
                aggregateRoot.replayEvents(eventStreams)
                aggregateRoot
            }
        }
    }

    private fun <T : IAggregateRoot?> rebuildAggregateRoot(aggregateRootType: Class<T>, eventStreams: List<DomainEventStream>?): T? {
        if (eventStreams == null || eventStreams.isEmpty()) {
            return null
        }
        val aggregateRoot = aggregateRootFactory.createAggregateRoot(aggregateRootType)
        aggregateRoot!!.replayEvents(eventStreams)
        return aggregateRoot
    }

    companion object {
        private const val MINVERSION = 1
        private const val MAXVERSION = Int.MAX_VALUE
    }
}
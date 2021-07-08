package org.enodeframework.eventing.impl

import org.enodeframework.eventing.IPublishedVersionStore
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author anruence@gmail.com
 */
class InMemoryPublishedVersionStore : IPublishedVersionStore {
    private val versionDict: ConcurrentMap<String, Int> = ConcurrentHashMap()
    override fun updatePublishedVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String,
        publishedVersion: Int
    ): CompletableFuture<Int> {
        versionDict[buildKey(processorName, aggregateRootId)] = publishedVersion
        return CompletableFuture.completedFuture(1)
    }

    override fun getPublishedVersionAsync(
        processorName: String,
        aggregateRootTypeName: String,
        aggregateRootId: String
    ): CompletableFuture<Int> {
        val publishedVersion = versionDict.getOrDefault(buildKey(processorName, aggregateRootId), 0)
        return CompletableFuture.completedFuture(publishedVersion)
    }

    private fun buildKey(eventProcessorName: String, aggregateRootId: String): String {
        return String.format("%s-%s", eventProcessorName, aggregateRootId)
    }
}
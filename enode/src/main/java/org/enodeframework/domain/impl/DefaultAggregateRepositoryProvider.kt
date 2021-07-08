package org.enodeframework.domain.impl

import org.enodeframework.common.container.ObjectContainer
import org.enodeframework.domain.IAggregateRepository
import org.enodeframework.domain.IAggregateRepositoryProvider
import org.enodeframework.domain.IAggregateRepositoryProxy
import org.enodeframework.domain.IAggregateRoot
import org.enodeframework.infrastructure.IAssemblyInitializer
import org.enodeframework.infrastructure.TypeUtils
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

/**
 * @author anruence@gmail.com
 */
class DefaultAggregateRepositoryProvider : IAggregateRepositoryProvider, IAssemblyInitializer {
    private val repositoryDict: MutableMap<Class<*>, IAggregateRepositoryProxy> = HashMap()
    override fun getRepository(aggregateRootType: Class<out IAggregateRoot>): IAggregateRepositoryProxy? {
        return repositoryDict[aggregateRootType]
    }

    override fun initialize(componentTypes: Set<Class<*>>) {
        componentTypes.stream().filter { type: Class<*> -> TypeUtils.isAggregateRepositoryType(type) }
            .forEach { aggregateRepositoryType: Class<*> -> registerAggregateRepository(aggregateRepositoryType) }
    }

    /**
     * 获取继承AggregateRoot的class，IAggregateRepository接口的泛型
     */
    private fun registerAggregateRepository(aggregateRepositoryType: Class<*>) {
        val genericInterfaces = aggregateRepositoryType.genericInterfaces
        Arrays.stream(genericInterfaces).forEach { x: Type ->
            val superGenericInterfaceType = x as ParameterizedType
            if (IAggregateRepository::class.java != superGenericInterfaceType.rawType) {
                return@forEach
            }
            val aggregateRepositoryProxy = AggregateRepositoryProxy<IAggregateRoot>()
            aggregateRepositoryProxy.setInnerObject(ObjectContainer.resolve(aggregateRepositoryType))
            repositoryDict[superGenericInterfaceType.actualTypeArguments[0] as Class<*>] = aggregateRepositoryProxy
        }
    }
}
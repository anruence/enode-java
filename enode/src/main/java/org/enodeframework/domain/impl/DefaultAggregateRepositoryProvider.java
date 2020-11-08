package org.enodeframework.domain.impl;

import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.domain.IAggregateRepository;
import org.enodeframework.domain.IAggregateRepositoryProvider;
import org.enodeframework.domain.IAggregateRepositoryProxy;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.infrastructure.IAssemblyInitializer;
import org.enodeframework.infrastructure.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author anruence@gmail.com
 */
public class DefaultAggregateRepositoryProvider implements IAggregateRepositoryProvider, IAssemblyInitializer {

    private final Map<Class<?>, IAggregateRepositoryProxy> repositoryDict = new HashMap<>();

    @Override
    public IAggregateRepositoryProxy getRepository(Class<? extends IAggregateRoot> aggregateRootType) {
        return repositoryDict.get(aggregateRootType);
    }

    @Override
    public void initialize(Set<Class<?>> componentTypes) {
        componentTypes.stream().filter(TypeUtils::isAggregateRepositoryType).forEach(this::registerAggregateRepository);
    }

    /**
     * 获取继承AggregateRoot的class，IAggregateRepository接口的泛型
     *
     * @param aggregateRepositoryType
     */
    private void registerAggregateRepository(Class<?> aggregateRepositoryType) {
        Type[] genericInterfaces = aggregateRepositoryType.getGenericInterfaces();
        Arrays.stream(genericInterfaces).forEach(x -> {
            ParameterizedType superGenericInterfaceType = (ParameterizedType) x;
            if (!IAggregateRepository.class.equals(superGenericInterfaceType.getRawType())) {
                return;
            }
            IAggregateRepository resolve = (IAggregateRepository) ObjectContainer.INSTANCE.resolve(aggregateRepositoryType);
            AggregateRepositoryProxy aggregateRepositoryProxy = new AggregateRepositoryProxy<>(resolve);
            repositoryDict.put((Class<?>) superGenericInterfaceType.getActualTypeArguments()[0], aggregateRepositoryProxy);
        });
    }
}

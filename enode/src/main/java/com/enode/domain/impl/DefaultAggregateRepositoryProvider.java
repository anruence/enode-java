package com.enode.domain.impl;

import com.enode.common.container.IObjectContainer;
import com.enode.domain.IAggregateRepository;
import com.enode.domain.IAggregateRepositoryProvider;
import com.enode.domain.IAggregateRepositoryProxy;
import com.enode.domain.IAggregateRoot;
import com.enode.infrastructure.IAssemblyInitializer;
import com.enode.infrastructure.TypeUtils;

import javax.inject.Inject;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultAggregateRepositoryProvider implements IAggregateRepositoryProvider, IAssemblyInitializer {
    @Inject
    private IObjectContainer objectContainer;
    private final Map<Class, IAggregateRepositoryProxy> _repositoryDict = new HashMap<>();

    @Override
    public IAggregateRepositoryProxy getRepository(Class<? extends IAggregateRoot> aggregateRootType) {
        return _repositoryDict.get(aggregateRootType);
    }

    @Override
    public void initialize(Set<Class<?>> componentTypes) {
        componentTypes.stream().filter(this::isAggregateRepositoryType).forEach(this::registerAggregateRepository);
    }

    private void registerAggregateRepository(Class aggregateRepositoryType) {
        Type superGenericInterface = TypeUtils.getSuperGenericInterface(aggregateRepositoryType, IAggregateRepository.class);

        if (superGenericInterface instanceof Class) {
            return;
        }

        ParameterizedType superGenericInterfaceType = (ParameterizedType) superGenericInterface;

        IAggregateRepository resolve = (IAggregateRepository) objectContainer.resolve(aggregateRepositoryType);

        AggregateRepositoryProxy<IAggregateRoot> aggregateRepositoryProxy = new AggregateRepositoryProxy<>(resolve);

        _repositoryDict.put((Class) superGenericInterfaceType.getActualTypeArguments()[0], aggregateRepositoryProxy);
    }

    private boolean isAggregateRepositoryType(Class type) {
        return type != null && !Modifier.isAbstract(type.getModifiers()) && IAggregateRepository.class.isAssignableFrom(type);
    }
}

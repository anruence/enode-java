package org.enodeframework.infrastructure

interface IAssemblyInitializer {
    fun initialize(componentTypes: Set<Class<*>>)
}
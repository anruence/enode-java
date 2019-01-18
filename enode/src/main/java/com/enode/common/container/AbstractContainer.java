package com.enode.common.container;

import com.google.inject.Provider;

import java.util.Properties;

public class AbstractContainer<C extends AbstractContainer<C>> {
    private IObjectContainer objectContainer;

    protected C setContainer(IObjectContainer objectContainer) {
        this.objectContainer = objectContainer;
        return (C) this;
    }

    public IObjectContainer getContainer() {
        return objectContainer;
    }

    public C commitRegisters() {
        objectContainer.commitRegisters();
        return (C) this;
    }

    public C registerStaticInjection(Class staticInjectionClass) {
        objectContainer.registerStaticInjection(staticInjectionClass);
        return (C) this;
    }

    public C override(Class implementationType, String serviceName, LifeStyle life) {
        objectContainer.override(implementationType, serviceName, life);
        return (C) this;
    }

    public C override(Class implementationType, String serviceName) {
        objectContainer.override(implementationType, serviceName);
        return (C) this;
    }

    public C override(Class implementationType) {
        objectContainer.override(implementationType);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C override(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName, LifeStyle life) {
        objectContainer.override(serviceType, implementationType, serviceName, life);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C override(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName) {
        objectContainer.override(serviceType, implementationType, serviceName);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C override(Class<TService> serviceType, Class<TImplementer> implementationType) {
        objectContainer.override(serviceType, implementationType);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C override(GenericTypeLiteral<TService> typeLiteral, Class<TImplementer> implementerType) {
        objectContainer.override(typeLiteral, implementerType);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C override(GenericTypeLiteral<TService> typeLiteral, GenericTypeLiteral<TImplementer> implementerTypeLiteral, String serviceName, LifeStyle life) {
        objectContainer.override(typeLiteral, implementerTypeLiteral, serviceName, life);
        return (C) this;
    }

    public <TService> C override(Class<TService> serviceType, String serviceName, Provider<TService> provider, LifeStyle life) {
        objectContainer.override(serviceType, serviceName, provider, life);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C overrideInstance(Class<TService> serviceType, TImplementer instance, String serviceName) {
        objectContainer.overrideInstance(serviceType, instance, serviceName);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C overrideInstance(Class<TService> serviceType, TImplementer instance) {
        objectContainer.overrideInstance(serviceType, instance);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C overrideInstance(GenericTypeLiteral<TService> typeLiteral, TImplementer instance) {
        objectContainer.overrideInstance(typeLiteral, instance);
        return (C) this;
    }

    public C register(Class implementationType, String serviceName, LifeStyle life) {
        objectContainer.register(implementationType, serviceName, life);
        return (C) this;
    }

    public C register(Class implementationType, String serviceName) {
        objectContainer.register(implementationType, serviceName);
        return (C) this;
    }

    public C register(Class implementationType) {
        objectContainer.register(implementationType);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C register(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName, LifeStyle life) {
        objectContainer.register(serviceType, implementationType, serviceName, life);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C register(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName) {
        objectContainer.register(serviceType, implementationType, serviceName);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C register(Class<TService> serviceType, Class<TImplementer> implementationType) {
        objectContainer.register(serviceType, implementationType);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C register(GenericTypeLiteral<TService> typeLiteral, Class<TImplementer> implementerType) {
        objectContainer.register(typeLiteral, implementerType);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C register(GenericTypeLiteral<TService> typeLiteral, GenericTypeLiteral<TImplementer> implementerTypeLiteral, String serviceName, LifeStyle life) {
        objectContainer.register(typeLiteral, implementerTypeLiteral, serviceName, life);
        return (C) this;
    }

    public <TService> C register(Class<TService> serviceType, String serviceName, Provider<TService> provider, LifeStyle life) {
        objectContainer.register(serviceType, serviceName, provider, life);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C registerInstance(Class<TService> serviceType, TImplementer instance, String serviceName) {
        objectContainer.registerInstance(serviceType, instance, serviceName);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C registerInstance(Class<TService> serviceType, TImplementer instance) {
        objectContainer.registerInstance(serviceType, instance);
        return (C) this;
    }

    public <TService, TImplementer extends TService> C registerInstance(GenericTypeLiteral<TService> typeLiteral, TImplementer instance) {
        objectContainer.registerInstance(typeLiteral, instance);
        return (C) this;
    }

    public C registerProperties(Properties properties) {
        objectContainer.registerProperties(properties);
        return (C) this;
    }

    public <TService> TService resolve(Class<TService> serviceType) {
        return objectContainer.resolve(serviceType);
    }

    public <TService> TService resolve(GenericTypeLiteral<TService> superTypeLiteral) {
        return objectContainer.resolve(superTypeLiteral);
    }

    public <TService> TService resolveNamed(Class<TService> serviceType, String serviceName) {
        return objectContainer.resolveNamed(serviceType, serviceName);
    }
}

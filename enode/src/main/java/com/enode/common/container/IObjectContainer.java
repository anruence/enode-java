package com.enode.common.container;

import com.google.inject.Provider;

import java.util.Properties;

public interface IObjectContainer {
    boolean commitRegisters();

    void registerStaticInjection(Class staticInjectionClass);

    void override(Class implementationType, String serviceName, LifeStyle life);

    void override(Class implementationType, String serviceName);

    void override(Class implementationType);

    <TService, TImplementer extends TService> void override(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName, LifeStyle life);

    <TService, TImplementer extends TService> void override(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName);

    <TService, TImplementer extends TService> void override(Class<TService> serviceType, Class<TImplementer> implementationType);

    <TService, TImplementer extends TService> void override(GenericTypeLiteral<TService> typeLiteral, Class<TImplementer> implementerType);

    <TService, TImplementer extends TService> void override(GenericTypeLiteral<TService> typeLiteral, GenericTypeLiteral<TImplementer> implementerTypeLiteral, String serviceName, LifeStyle life);

    <TService> void override(Class<TService> serviceType, String serviceName, Provider<TService> provider, LifeStyle life);

    <TService, TImplementer extends TService> void overrideInstance(Class<TService> serviceType, TImplementer instance, String serviceName);

    <TService, TImplementer extends TService> void overrideInstance(Class<TService> serviceType, TImplementer instance);

    <TService, TImplementer extends TService> void overrideInstance(GenericTypeLiteral<TService> typeLiteral, TImplementer instance);

    /** ===================*/
    void register(Class implementationType, String serviceName, LifeStyle life);

    void register(Class implementationType, String serviceName);

    void register(Class implementationType);

    <TService, TImplementer extends TService> void register(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName, LifeStyle life);

    <TService, TImplementer extends TService> void register(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName);

    <TService, TImplementer extends TService> void register(Class<TService> serviceType, Class<TImplementer> implementationType);

    <TService, TImplementer extends TService> void register(GenericTypeLiteral<TService> typeLiteral, Class<TImplementer> implementerType);

    <TService, TImplementer extends TService> void register(GenericTypeLiteral<TService> typeLiteral, GenericTypeLiteral<TImplementer> implementerTypeLiteral, String serviceName, LifeStyle life);

    <TService> void register(Class<TService> serviceType, String serviceName, Provider<TService> provider, LifeStyle life);

    <TService, TImplementer extends TService> void registerInstance(Class<TService> serviceType, TImplementer instance, String serviceName);

    <TService, TImplementer extends TService> void registerInstance(Class<TService> serviceType, TImplementer instance);

    <TService, TImplementer extends TService> void registerInstance(GenericTypeLiteral<TService> typeLiteral, TImplementer instance);

    void registerProperties(Properties properties);

    <TService> TService resolve(Class<TService> serviceType);

    <TService> TService resolve(GenericTypeLiteral<TService> superTypeLiteral);

    //bool tryResolve(Type serviceType, out object instance);

    <TService> TService resolveNamed(Class<TService> serviceType, String serviceName);
}

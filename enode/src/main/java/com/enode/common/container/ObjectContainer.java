package com.enode.common.container;

import com.google.inject.Provider;

import java.util.Properties;

@Deprecated
public class ObjectContainer {
    public static IObjectContainer current;

    public static void setContainer(IObjectContainer container) {
        current = container;
    }

    public static boolean commitRegisters() {
        return current.commitRegisters();
    }

    public static void registerStaticInjection(Class staticInjectionClass) {
        current.registerStaticInjection(staticInjectionClass);
    }

    /*** ========== ioc override operation ========== */

    /**
     * ============  实现类注入 ===========
     */
    public static void override(Class implementationType, String serviceName, LifeStyle life) {
        current.override(implementationType, serviceName, life);
    }

    public static void override(Class implementationType, String serviceName) {
        current.override(implementationType, serviceName);
    }

    public static void override(Class implementationType) {
        current.override(implementationType);
    }

    /**
     * ============  接口--实现类注入 ===========
     */
    public static <TService, TImplementer extends TService> void override(Class<TService> serviceType, Class<TImplementer> implementionType, String serviceName, LifeStyle life) {
        current.override(serviceType, implementionType, serviceName, life);
    }

    public static <TService, TImplementer extends TService> void override(Class<TService> serviceType, Class<TImplementer> implementionType, String serviceName) {
        current.override(serviceType, implementionType, serviceName);
    }

    public static <TService, TImplementer extends TService> void override(Class<TService> serviceType, Class<TImplementer> implementionType) {
        current.override(serviceType, implementionType);
    }

    public static <TService, TImplementer extends TService> void override(GenericTypeLiteral<TService> genericTypeLiteral, Class<TImplementer> implementionType) {
        current.override(genericTypeLiteral, implementionType);
    }

    public static <TService, TImplementer extends TService> void override(GenericTypeLiteral<TService> typeLiteral, GenericTypeLiteral<TImplementer> implementerTypeLiteral, String serviceName, LifeStyle life) {
        current.override(typeLiteral, implementerTypeLiteral, serviceName, life);
    }

    public static <TService> void override(Class<TService> serviceType, String serviceName, Provider<TService> provider, LifeStyle life) {
        current.override(serviceType, serviceName, provider, life);
    }

    /**
     * ============  接口--实例类注入 ===========
     */
    public static <TService, TImplementer extends TService> void overrideInstance(Class<TService> serviceType, TImplementer instance, String serviceName) {
        current.overrideInstance(serviceType, instance, serviceName);
    }

    public static <TService, TImplementer extends TService> void overrideInstance(Class<TService> serviceType, TImplementer instance) {
        current.overrideInstance(serviceType, instance);
    }

    public static <TService, TImplementer extends TService> void overrideInstance(GenericTypeLiteral<TService> genericTypeLiteral, TImplementer instance) {
        current.overrideInstance(genericTypeLiteral, instance);
    }

    /*** ========== ioc register operation ========== */

    /**
     * ============  实现类注入 ===========
     */
    public static void register(Class implementationType, String serviceName, LifeStyle life) {
        current.register(implementationType, serviceName, life);
    }

    public static void register(Class implementationType, String serviceName) {
        current.register(implementationType, serviceName);
    }

    public static void register(Class implementationType) {
        current.register(implementationType);
    }

    /**
     * ============  接口--实现类注入 ===========
     */
    public static <TService, TImplementer extends TService> void register(Class<TService> serviceType, Class<TImplementer> implementionType, String serviceName, LifeStyle life) {
        current.register(serviceType, implementionType, serviceName, life);
    }

    public static <TService, TImplementer extends TService> void register(Class<TService> serviceType, Class<TImplementer> implementionType, String serviceName) {
        current.register(serviceType, implementionType, serviceName);
    }

    public static <TService, TImplementer extends TService> void register(Class<TService> serviceType, Class<TImplementer> implementionType) {
        current.register(serviceType, implementionType);
    }

    public static <TService, TImplementer extends TService> void register(GenericTypeLiteral<TService> genericTypeLiteral, Class<TImplementer> implementionType) {
        current.register(genericTypeLiteral, implementionType);
    }

    public static <TService, TImplementer extends TService> void register(GenericTypeLiteral<TService> typeLiteral, GenericTypeLiteral<TImplementer> implementerTypeLiteral, String serviceName, LifeStyle life) {
        current.register(typeLiteral, implementerTypeLiteral, serviceName, life);
    }

    public static <TService> void register(Class<TService> serviceType, String serviceName, Provider<TService> provider, LifeStyle life) {
        current.register(serviceType, serviceName, provider, life);
    }

    /**
     * ============  接口--实例类注入 ===========
     */
    public static <TService, TImplementer extends TService> void registerInstance(Class<TService> serviceType, TImplementer instance, String serviceName) {
        current.registerInstance(serviceType, instance, serviceName);
    }

    public static <TService, TImplementer extends TService> void registerInstance(Class<TService> serviceType, TImplementer instance) {
        current.registerInstance(serviceType, instance);
    }

    public static <TService, TImplementer extends TService> void registerInstance(GenericTypeLiteral<TService> genericTypeLiteral, TImplementer instance) {
        current.registerInstance(genericTypeLiteral, instance);
    }

    public static void registerProperties(Properties properties) {
        current.registerProperties(properties);
    }

    public static <TService> TService resolve(Class<TService> serviceType) {
        return current.resolve(serviceType);
    }

    public static <TService> TService tryResolve(Class<TService> serviceType) {
        try {
            return current.resolve(serviceType);
        } catch (Exception e) {
            return null;
        }
    }

    public static <TService> TService resolve(GenericTypeLiteral<TService> superTypeLiteral) {
        return current.resolve(superTypeLiteral);
    }

    public static <TService> TService tryResolve(GenericTypeLiteral<TService> superTypeLiteral) {
        try {
            return current.resolve(superTypeLiteral);
        } catch (Exception e) {
            return null;
        }
    }

    public static <TService> TService resolveNamed(Class<TService> serviceType, String serviceName) {
        return current.resolveNamed(serviceType, serviceName);
    }
}

package com.enode.common.thirdparty.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.enode.common.container.GenericTypeLiteral;
import com.enode.common.container.IObjectContainer;
import com.enode.common.container.LifeStyle;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class GuiceObjectContainer implements IObjectContainer {
    private Map<Key, GuiceModule> iocMap = new HashMap<>();
    private List<Class> staticInjections = new ArrayList<>();
    private Injector container;
    private Module module;

    public GuiceObjectContainer() {
    }

    @Override
    public boolean commitRegisters() {
        if (!iocMap.isEmpty()) {
            override(new GuiceMapModule(container, iocMap, staticInjections));
            iocMap.clear();
            //staticInjections.clear(); 是否需要清除
            return true;
        }
        return false;
    }

    static class GuiceMapModule extends AbstractModule {
        private final static List<Key> GUICE_INNER_BINDINGS = new ArrayList<Key>() {{
            add(Key.get(Injector.class));
            add(Key.get(Logger.class));
            add(Key.get(Stage.class));
        }};
        private Map<Key, GuiceModule> iocMap;
        private List<Class> staticInjections = new ArrayList<>();
        private Injector parent;

        public GuiceMapModule(Injector parent, Map<Key, GuiceModule> iocMap, List<Class> staticInjections) {
            this.parent = parent;
            this.iocMap = new HashMap<>(iocMap);
            this.staticInjections = staticInjections;
        }

        @Override
        protected void configure() {
            if (parent != null) {
                parent.getBindings().entrySet().stream().filter(this::checkBind).forEach(entry -> {
                    Key<Object> key = (Key<Object>) entry.getKey();

                    bind(key).toProvider(entry.getValue().getProvider());
                });
            }
            staticInjections.forEach(clazz -> binder().requestStaticInjection(clazz));
            iocMap.values().forEach(binder -> binder.bind(binder()));
        }

        protected boolean checkBind(Map.Entry<Key<?>, Binding<?>> binderEntry) {
            Key<?> key = binderEntry.getKey();
            return !(isGuiceInnerBinder(key) || isOverride(key));
        }

        protected boolean isGuiceInnerBinder(Key key) {
            return GUICE_INNER_BINDINGS.stream().anyMatch(x -> key.equals(x));
        }

        protected boolean isOverride(Key key) {
            return iocMap.containsKey(key);
        }
    }

    /*private void override(Module newModule) {
        if (this.module == null) {
            this.module = newModule;
        } else {
            this.module = Modules.override(this.module).with(newModule);
        }
        this.container = Guice.createInjector(this.module);
    }*/
    private void override(Module newModule) {
        if (this.container == null) {
            this.container = Guice.createInjector(newModule);
        } else {
            //this.container = this.container.createChildInjector(newModule);
            this.container = Guice.createInjector(newModule);
        }
    }

    private void override(GuiceModule module) {
        register(module);
        commitRegisters();
    }

    private void register(GuiceModule module) {
        iocMap.put(module.key(), module);
    }

    @Override
    public void registerStaticInjection(Class staticInjectionClass) {
        staticInjections.add(staticInjectionClass);
    }

    @Override
    public void override(Class implementationType, String serviceName, LifeStyle life) {
        override(GuiceModule.implementionTypeModule(implementationType, serviceName, life));
    }

    @Override
    public void override(Class implementationType, String serviceName) {
        override(GuiceModule.implementionTypeModule(implementationType, serviceName, LifeStyle.Singleton));
    }

    @Override
    public void override(Class implementationType) {
        override(GuiceModule.implementionTypeModule(implementationType, null, LifeStyle.Singleton));
    }

    @Override
    public <TService, TImplementer extends TService> void override(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName, LifeStyle life) {
        override(GuiceModule.serviceTypeModule(serviceType, serviceName, implementationType, life));
    }

    @Override
    public <TService, TImplementer extends TService> void override(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName) {
        override(GuiceModule.serviceTypeModule(serviceType, serviceName, implementationType, LifeStyle.Singleton));
    }

    @Override
    public <TService, TImplementer extends TService> void override(Class<TService> serviceType, Class<TImplementer> implementationType) {
        override(GuiceModule.serviceTypeModule(serviceType, null, implementationType, LifeStyle.Singleton));
    }

    @Override
    public <TService, TImplementer extends TService> void override(GenericTypeLiteral<TService> typeLiteral, Class<TImplementer> implementerType) {
        override(GuiceModule.typeLiteralTypeModule(typeLiteral, implementerType, LifeStyle.Singleton));
    }

    @Override
    public <TService, TImplementer extends TService> void override(GenericTypeLiteral<TService> typeLiteral, GenericTypeLiteral<TImplementer> implementerTypeLiteral, String serviceName, LifeStyle life) {
        override(GuiceModule.typeLiteral2TypeLiteralModule(typeLiteral, implementerTypeLiteral, serviceName, life));
    }

    @Override
    public <TService> void override(Class<TService> serviceType, String serviceName, Provider<TService> provider, LifeStyle life) {
        override(GuiceModule.providerModule(serviceType, serviceName, provider, life));
    }

    @Override
    public <TService, TImplementer extends TService> void overrideInstance(Class<TService> serviceType, TImplementer instance, String serviceName) {
        override(GuiceModule.instanceModule(serviceType, serviceName, instance));
    }

    @Override
    public <TService, TImplementer extends TService> void overrideInstance(Class<TService> serviceType, TImplementer instance) {
        override(GuiceModule.instanceModule(serviceType, null, instance));
    }

    @Override
    public <TService, TImplementer extends TService> void overrideInstance(GenericTypeLiteral<TService> typeLiteral, TImplementer instance) {
        override(GuiceModule.typeLiteralInstanceModule(typeLiteral, instance));
    }

    /***
     * ==========================
     */
    @Override
    public void register(Class implementationType, String serviceName, LifeStyle life) {
        register(GuiceModule.implementionTypeModule(implementationType, serviceName, life));
    }

    @Override
    public void register(Class implementationType, String serviceName) {
        register(GuiceModule.implementionTypeModule(implementationType, serviceName, LifeStyle.Singleton));
    }

    @Override
    public void register(Class implementationType) {
        register(GuiceModule.implementionTypeModule(implementationType, null, LifeStyle.Singleton));
    }

    @Override
    public <TService, TImplementer extends TService> void register(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName, LifeStyle life) {
        register(GuiceModule.serviceTypeModule(serviceType, serviceName, implementationType, life));
    }

    @Override
    public <TService, TImplementer extends TService> void register(Class<TService> serviceType, Class<TImplementer> implementationType, String serviceName) {
        register(GuiceModule.serviceTypeModule(serviceType, serviceName, implementationType, LifeStyle.Singleton));
    }

    @Override
    public <TService, TImplementer extends TService> void register(Class<TService> serviceType, Class<TImplementer> implementationType) {
        register(GuiceModule.serviceTypeModule(serviceType, null, implementationType, LifeStyle.Singleton));
    }

    @Override
    public <TService, TImplementer extends TService> void register(GenericTypeLiteral<TService> typeLiteral, Class<TImplementer> implementerType) {
        register(GuiceModule.typeLiteralTypeModule(typeLiteral, implementerType, LifeStyle.Singleton));
    }

    @Override
    public <TService, TImplementer extends TService> void register(GenericTypeLiteral<TService> typeLiteral, GenericTypeLiteral<TImplementer> implementerTypeLiteral, String serviceName, LifeStyle life) {
        register(GuiceModule.typeLiteral2TypeLiteralModule(typeLiteral, implementerTypeLiteral, serviceName, life));
    }

    @Override
    public <TService> void register(Class<TService> serviceType, String serviceName, Provider<TService> provider, LifeStyle life) {
        register(GuiceModule.providerModule(serviceType, serviceName, provider, life));
    }

    @Override
    public <TService, TImplementer extends TService> void registerInstance(Class<TService> serviceType, TImplementer instance, String serviceName) {
        register(GuiceModule.instanceModule(serviceType, serviceName, instance));
    }

    @Override
    public <TService, TImplementer extends TService> void registerInstance(Class<TService> serviceType, TImplementer instance) {
        register(GuiceModule.instanceModule(serviceType, null, instance));
    }

    @Override
    public <TService, TImplementer extends TService> void registerInstance(GenericTypeLiteral<TService> typeLiteral, TImplementer instance) {
        register(GuiceModule.typeLiteralInstanceModule(typeLiteral, instance));
    }

    @Override
    public void registerProperties(Properties properties) {
        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
            String propertyName = (String) e.nextElement();
            String value = properties.getProperty(propertyName);

            registerInstance(String.class, value, propertyName);
        }
    }

    @Override
    public <TService> TService resolve(Class<TService> serviceType) {
        return container.getInstance(serviceType);
    }

    @Override
    public <TService> TService resolve(GenericTypeLiteral<TService> superTypeLiteral) {
        TypeLiteral<TService> typeLiteral = TypeLiteralConverter.convert(superTypeLiteral);

        return container.getInstance(Key.get(typeLiteral));
    }

    @Override
    public <TService> TService resolveNamed(Class<TService> serviceType, String serviceName) {
        return container.getInstance(Key.get(serviceType, Names.named(serviceName)));
    }
}

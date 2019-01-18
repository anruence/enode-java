package com.enode.common.thirdparty.guice;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.enode.common.container.GenericTypeLiteral;
import com.enode.common.container.LifeStyle;

public interface GuiceModule<T> {

    static <TService, TImplementer extends TService> GuiceModule serviceTypeModule(Class<TService> serviceType, String serviceName,
                                                                                   Class<TImplementer> implementionType, LifeStyle life) {
        return new ServiceTypeModule<>(serviceType, serviceName, implementionType, life);
    }

    static GuiceModule implementionTypeModule(Class implementionType, String serviceName, LifeStyle life) {
        return new ImplementionTypeModule(implementionType, serviceName, life);
    }

    static <TService, TImplementer extends TService> GuiceModule instanceModule(Class<TService> serviceType, String serviceName, TImplementer instance) {
        return new InstanceModule<>(serviceType, serviceName, instance);
    }

    static <TService, TImplementer extends TService> GuiceModule typeLiteralTypeModule(GenericTypeLiteral<TService> superTypeLiteral, Class<TImplementer> implementionType, LifeStyle life) {
        return new TypeLiteralTypeModule<>(superTypeLiteral, implementionType, life);
    }

    static <TService, TImplementer extends TService> GuiceModule typeLiteral2TypeLiteralModule(GenericTypeLiteral<TService> superTypeLiteral, GenericTypeLiteral<TImplementer> implementerGenericTypeLiteral, String serviceName, LifeStyle life) {
        return new TypeLiteral2TypeLiteralModule<>(superTypeLiteral, implementerGenericTypeLiteral, serviceName, life);
    }

    static <TService, TImplementer extends TService> GuiceModule typeLiteralInstanceModule(GenericTypeLiteral<TService> genericTypeLiteral, TImplementer instance) {
        return new TypeLiteralInstanceModule<>(genericTypeLiteral, instance);
    }

    static <TService> GuiceModule providerModule(Class<TService> serviceType, String serviceName,
                                                 Provider<TService> provider, LifeStyle life) {
        return new ProviderModule<>(serviceType, serviceName, provider, life);
    }

    void bind(Binder binder);

    Module toModule();

    Key<T> key();

    abstract class AbstractGuiceModule<T> implements GuiceModule<T> {
        @Override
        public Module toModule() {
            return new AbstractModule() {
                @Override
                protected void configure() {
                    AbstractGuiceModule.this.bind(binder());
                }
            };
        }
    }

    class TypeLiteralTypeModule<TService, TImplementer extends TService> extends AbstractGuiceModule<TService> {

        private GenericTypeLiteral<TService> superTypeLiteral;
        private Class<TImplementer> implementionType;
        private LifeStyle life;

        protected TypeLiteralTypeModule(GenericTypeLiteral<TService> superTypeLiteral, Class<TImplementer> implementionType, LifeStyle life) {
            this.superTypeLiteral = superTypeLiteral;
            this.implementionType = implementionType;
            this.life = life;
        }

        @Override
        public void bind(Binder binder) {
            TypeLiteral<TService> typeLiteral = TypeLiteralConverter.convert(superTypeLiteral);
            binder.bind(typeLiteral).to(implementionType).in(ScopeConverter.toGuiceScope(life));
        }

        @Override
        public Key<TService> key() {
            return Key.get(TypeLiteralConverter.convert(superTypeLiteral));
        }
    }

    class TypeLiteral2TypeLiteralModule<TService, TImplementer extends TService> extends AbstractGuiceModule<TService> {
        private GenericTypeLiteral<TService> superTypeLiteral;
        private GenericTypeLiteral<TImplementer> implementionTypeLiteral;
        private String serviceName;
        private LifeStyle life;

        public TypeLiteral2TypeLiteralModule(GenericTypeLiteral<TService> superTypeLiteral, GenericTypeLiteral<TImplementer> implementionTypeLiteral, String serviceName, LifeStyle life) {
            this.superTypeLiteral = superTypeLiteral;
            this.implementionTypeLiteral = implementionTypeLiteral;
            this.serviceName = serviceName;
            this.life = life;
        }

        @Override
        public void bind(Binder binder) {
            TypeLiteral<TService> superType = TypeLiteralConverter.convert(superTypeLiteral);
            TypeLiteral<TImplementer> implementerType = TypeLiteralConverter.convert(implementionTypeLiteral);


            if (serviceName == null) {
                binder.bind(superType).to(implementerType).in(ScopeConverter.toGuiceScope(life));
            } else {
                binder.bind(Key.get(superType, Names.named(serviceName))).to(implementerType).in(ScopeConverter.toGuiceScope(life));
            }
        }

        @Override
        public Key<TService> key() {
            if (serviceName == null) {
                return Key.get(TypeLiteralConverter.convert(superTypeLiteral));
            } else {
                return Key.get(TypeLiteralConverter.convert(superTypeLiteral), Names.named(serviceName));
            }
        }
    }

    class TypeLiteralInstanceModule<TService, TImplementer extends TService> extends AbstractGuiceModule<TService> {
        private GenericTypeLiteral<TService> superTypeLiteral;
        private TImplementer instance;

        protected TypeLiteralInstanceModule(GenericTypeLiteral<TService> superTypeLiteral, TImplementer instance) {
            this.superTypeLiteral = superTypeLiteral;
            this.instance = instance;
        }

        @Override
        public void bind(Binder binder) {
            TypeLiteral<TService> typeLiteral = TypeLiteralConverter.convert(superTypeLiteral);
            binder.bind(typeLiteral).toInstance(instance);
        }

        @Override
        public Key<TService> key() {
            return Key.get(TypeLiteralConverter.convert(superTypeLiteral));
        }
    }

    class ServiceTypeModule<TService, TImplementer extends TService> extends AbstractGuiceModule<TService> {

        private Class<TService> serviceType;
        private String serviceName;
        private Class<TImplementer> implementionType;
        private LifeStyle life;

        protected ServiceTypeModule(Class<TService> serviceType, String serviceName, Class<TImplementer> implementionType, LifeStyle life) {
            this.serviceName = serviceName;
            this.serviceType = serviceType;
            this.implementionType = implementionType;
            this.life = life;
        }

        @Override
        public void bind(Binder binder) {
            if (serviceName == null) {
                binder.bind(serviceType).to(implementionType).in(ScopeConverter.toGuiceScope(life));
            } else {
                binder.bind(Key.get(serviceType, Names.named(serviceName))).to(implementionType).in(ScopeConverter.toGuiceScope(life));
            }
        }

        @Override
        public Key<TService> key() {
            if (serviceName == null) {
                return Key.get(serviceType);
            } else {
                return Key.get(serviceType, Names.named(serviceName));
            }
        }
    }

    class ImplementionTypeModule<TImplementer> extends AbstractGuiceModule<TImplementer> {

        private Class<TImplementer> implementionType;
        private String serviceName;
        private LifeStyle life;

        protected ImplementionTypeModule(Class<TImplementer> implementionType, String serviceName, LifeStyle life) {
            this.implementionType = implementionType;
            this.serviceName = serviceName;
            this.life = life;
        }

        @Override
        public void bind(Binder binder) {
            if (serviceName == null) {
                binder.bind(implementionType).in(ScopeConverter.toGuiceScope(life));
            } else {
                binder.bind(Key.get(implementionType, Names.named(serviceName))).in(ScopeConverter.toGuiceScope(life));
            }
        }

        @Override
        public Key<TImplementer> key() {
            if (serviceName == null) {
                return Key.get(implementionType);
            } else {
                return Key.get(implementionType, Names.named(serviceName));
            }
        }
    }

    class InstanceModule<TService, TImplementer extends TService> extends AbstractGuiceModule<TService> {
        private Class<TService> serviceType;
        private String serviceName;
        private TImplementer instance;

        protected InstanceModule(Class<TService> serviceType, String serviceName, TImplementer instance) {
            this.serviceType = serviceType;
            this.serviceName = serviceName;
            this.instance = instance;
        }

        @Override
        public void bind(Binder binder) {
            if (serviceName == null) {
                binder.bind(serviceType).toInstance(instance);
            } else {
                binder.bind(Key.get(serviceType, Names.named(serviceName))).toInstance(instance);
            }
        }

        @Override
        public Key<TService> key() {
            if (serviceName == null) {
                return Key.get(serviceType);
            } else {
                return Key.get(serviceType, Names.named(serviceName));
            }
        }
    }

    class ProviderModule<TService> extends AbstractGuiceModule<TService> {
        private Class<TService> serviceType;
        private String serviceName;
        private Provider<TService> provider;
        private LifeStyle life;

        public ProviderModule(Class<TService> serviceType, String serviceName, Provider<TService> provider, LifeStyle life) {
            this.serviceType = serviceType;
            this.serviceName = serviceName;
            this.provider = provider;
            this.life = life;
        }

        @Override
        public void bind(Binder binder) {
            if (serviceName == null) {
                binder.bind(serviceType).toProvider(provider).in(ScopeConverter.toGuiceScope(life));
            } else {
                binder.bind(Key.get(serviceType, Names.named(serviceName))).toProvider(provider).in(ScopeConverter.toGuiceScope(life));
            }
        }

        @Override
        public Key<TService> key() {
            if (serviceName == null) {
                return Key.get(serviceType);
            } else {
                return Key.get(serviceType, Names.named(serviceName));
            }
        }
    }
}

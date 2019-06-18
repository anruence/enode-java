package com.enodeframework;

import com.enodeframework.commanding.impl.DefaultCommandAsyncHandlerProvider;
import com.enodeframework.commanding.impl.DefaultCommandHandlerProvider;
import com.enodeframework.common.extensions.ClassNameComparator;
import com.enodeframework.common.extensions.ClassPathScanHandler;
import com.enodeframework.domain.impl.DefaultAggregateRepositoryProvider;
import com.enodeframework.domain.impl.DefaultAggregateRootInternalHandlerProvider;
import com.enodeframework.infrastructure.WrappedRuntimeException;
import com.enodeframework.infrastructure.impl.DefaultMessageHandlerProvider;
import com.enodeframework.infrastructure.impl.DefaultThreeMessageHandlerProvider;
import com.enodeframework.infrastructure.impl.DefaultTwoMessageHandlerProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author anruence@gmail.com
 * 应用的核心引导启动类
 * 负责扫描在applicationContext.xml中配置的packages. 获取到Command，Event
 */
public class ENodeBootstrap {

    private List<String> packages;

    private ClassPathScanHandler handler;

    @Autowired
    private DefaultCommandAsyncHandlerProvider commandAsyncHandlerProvider;

    @Autowired
    private DefaultCommandHandlerProvider commandHandlerProvider;

    @Autowired
    private DefaultMessageHandlerProvider messageHandlerProvider;

    @Autowired
    private DefaultTwoMessageHandlerProvider twoMessageHandlerProvider;

    @Autowired
    private DefaultThreeMessageHandlerProvider threeMessageHandlerProvider;

    @Autowired
    private DefaultAggregateRepositoryProvider aggregateRepositoryProvider;

    @Autowired
    private DefaultAggregateRootInternalHandlerProvider aggregateRootInternalHandlerProvider;

    public void init() {
        Set<Class<?>> classSet = scanConfiguredPackages();
        registerBeans(classSet);
    }

    /**
     * @param classSet
     */
    private void registerBeans(Set<Class<?>> classSet) {
        commandAsyncHandlerProvider.initialize(classSet);
        commandHandlerProvider.initialize(classSet);
        messageHandlerProvider.initialize(classSet);
        twoMessageHandlerProvider.initialize(classSet);
        threeMessageHandlerProvider.initialize(classSet);
        aggregateRepositoryProvider.initialize(classSet);
        aggregateRootInternalHandlerProvider.initialize(classSet);
    }

    /**
     * Scan the packages configured in Spring xml
     */
    private Set<Class<?>> scanConfiguredPackages() {
        if (packages == null) {
            throw new WrappedRuntimeException("Command packages is not specified");
        }
        String[] pkgs = new String[packages.size()];
        handler = new ClassPathScanHandler(packages.toArray(pkgs));
        Set<Class<?>> classSet = new TreeSet<>(new ClassNameComparator());
        for (String pakName : packages) {
            classSet.addAll(handler.getPackageAllClasses(pakName, true));
        }
        return classSet;
    }

    public List<String> getPackages() {
        return this.packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }
}

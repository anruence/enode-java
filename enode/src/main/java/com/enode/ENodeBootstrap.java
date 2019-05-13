package com.enode;

import com.enode.commanding.impl.DefaultCommandAsyncHandlerProvider;
import com.enode.commanding.impl.DefaultCommandHandlerProvider;
import com.enode.common.extensions.ClassNameComparator;
import com.enode.common.extensions.ClassPathScanHandler;
import com.enode.common.extensions.ENodeException;
import com.enode.domain.impl.DefaultAggregateRepositoryProvider;
import com.enode.domain.impl.DefaultAggregateRootInternalHandlerProvider;
import com.enode.infrastructure.impl.DefaultMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultThreeMessageHandlerProvider;
import com.enode.infrastructure.impl.DefaultTwoMessageHandlerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 应用的核心引导启动类
 * 负责扫描在applicationContext.xml中配置的packages. 获取到Command，Event
 */
@Import(value = {ENodeAutoConfiguration.class, InMemoryConfiguration.class})
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
     *
     * @return
     */
    private Set<Class<?>> scanConfiguredPackages() {
        if (packages == null) {
            throw new ENodeException("Command packages is not specified");
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

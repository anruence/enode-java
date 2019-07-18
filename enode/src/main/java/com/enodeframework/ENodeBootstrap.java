package com.enodeframework;

import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.common.extensions.ClassNameComparator;
import com.enodeframework.common.extensions.ClassPathScanHandler;
import com.enodeframework.infrastructure.IAssemblyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 应用的核心引导启动类
 * 负责扫描需要注册的packages. 获取到Command，Event
 *
 * @author anruence@gmail.com
 */
public class ENodeBootstrap {

    private static Logger logger = LoggerFactory.getLogger(ENodeBootstrap.class);

    private List<String> packages;

    @Autowired
    private IObjectContainer objectContainer;

    public void init() {
        Set<Class<?>> classSet = scanConfiguredPackages();
        registerBeans(classSet);
    }

    /**
     * @param classSet
     */
    private void registerBeans(Set<Class<?>> classSet) {
        objectContainer.resolveAll(IAssemblyInitializer.class).values().forEach(provider -> {
            provider.initialize(classSet);
            if (logger.isDebugEnabled()) {
                logger.debug("{} initial success", provider.getClass().getName());
            }
        });
    }

    /**
     * Scan the packages configured in Spring xml
     */
    private Set<Class<?>> scanConfiguredPackages() {
        if (packages == null) {
            throw new IllegalArgumentException("Command packages is not specified");
        }
        String[] pkgs = new String[packages.size()];
        ClassPathScanHandler handler = new ClassPathScanHandler(packages.toArray(pkgs));
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

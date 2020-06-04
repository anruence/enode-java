package org.enodeframework;

import org.enodeframework.common.container.IObjectContainer;
import org.enodeframework.common.extensions.ClassNameComparator;
import org.enodeframework.common.extensions.ClassPathScanHandler;
import org.enodeframework.infrastructure.IAssemblyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 应用的核心引导启动，负责扫描需要注册的scanPackages. 获取到Command，Event
 *
 * @author anruence@gmail.com
 */
public class ENodeBootstrap {

    private final static Logger logger = LoggerFactory.getLogger(ENodeBootstrap.class);

    private List<String> scanPackages;

    @Autowired
    private IObjectContainer objectContainer;

    public void init() {
        Set<Class<?>> classSet = scanConfiguredPackages();
        registerBeans(classSet);
    }

    /**
     *
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
        if (scanPackages == null) {
            throw new IllegalArgumentException("Command packages is not specified");
        }
        String[] pkgs = new String[scanPackages.size()];
        ClassPathScanHandler handler = new ClassPathScanHandler(scanPackages.toArray(pkgs));
        Set<Class<?>> classSet = new TreeSet<>(new ClassNameComparator());
        for (String pakName : scanPackages) {
            classSet.addAll(handler.getPackageAllClasses(pakName, true));
        }
        return classSet;
    }

    public List<String> getScanPackages() {
        return this.scanPackages;
    }

    public void setScanPackages(List<String> scanPackages) {
        this.scanPackages = scanPackages;
    }
}

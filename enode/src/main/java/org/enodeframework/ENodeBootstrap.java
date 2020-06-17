package org.enodeframework;

import org.enodeframework.common.container.SpringObjectContainer;
import org.enodeframework.common.extensions.ClassNameComparator;
import org.enodeframework.common.extensions.ClassPathScanHandler;
import org.enodeframework.infrastructure.IAssemblyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Set;
import java.util.TreeSet;

/**
 * 应用的核心引导启动，负责扫描需要注册的scanPackages. 获取到Command，Event
 *
 * @author anruence@gmail.com
 */
public class ENodeBootstrap implements ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(ENodeBootstrap.class);

    private final String[] scanPackages;

    private ApplicationContext applicationContext;

    public ENodeBootstrap(String... scanPackages) {
        this.scanPackages = scanPackages;
    }

    public void init() {
        Set<Class<?>> classSet = scanConfiguredPackages();
        registerBeans(classSet);
    }

    private void registerBeans(Set<Class<?>> classSet) {
        applicationContext.getBeansOfType(IAssemblyInitializer.class).values().forEach(provider -> {
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
            throw new IllegalArgumentException("packages is not specified");
        }
        ClassPathScanHandler handler = new ClassPathScanHandler(scanPackages);
        Set<Class<?>> classSet = new TreeSet<>(new ClassNameComparator());
        for (String pakName : scanPackages) {
            classSet.addAll(handler.getPackageAllClasses(pakName, true));
        }
        return classSet;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        SpringObjectContainer objectContainer = new SpringObjectContainer(applicationContext);
        ObjectContainer.container = objectContainer;
    }
}
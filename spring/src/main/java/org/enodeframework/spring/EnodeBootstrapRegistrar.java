package org.enodeframework.spring;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Event;
import org.enodeframework.common.container.ObjectContainer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An {@link ImportBeanDefinitionRegistrar} class that registers a bean
 * capable of processing Spring's @{@link Command} and @{@link Event} annotation.
 * <p>This configuration class is automatically imported when using the @{@link EnableEnode}
 * annotation.  See {@link EnableEnode} Javadoc for complete usage.
 *
 * @see EnableEnode
 */
public class EnodeBootstrapRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;

    private Environment environment;

    static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, String annotationClassName) {
        return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationClassName, false));
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        String declaringClass = metadata.getClass().getName();
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false, this.environment, this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Command.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Event.class));
        AnnotationAttributes enodeScan = attributesFor(metadata, EnableEnode.class.getName());
        Set<String> basePackages = new LinkedHashSet<>();
        String[] basePackagesArray = enodeScan.getStringArray("basePackages");
        String[] scanBasePackagesArray = enodeScan.getStringArray("scanBasePackages");
        for (String pkg : basePackagesArray) {
            String[] tokenized = StringUtils.tokenizeToStringArray(this.environment.resolvePlaceholders(pkg),
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            Collections.addAll(basePackages, tokenized);
        }
        for (String pkg : scanBasePackagesArray) {
            String[] tokenized = StringUtils.tokenizeToStringArray(this.environment.resolvePlaceholders(pkg),
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            Collections.addAll(basePackages, tokenized);
        }
        for (Class<?> clazz : enodeScan.getClassArray("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
        }
        scanner.addExcludeFilter(new AbstractTypeHierarchyTraversingFilter(false, false) {
            @Override
            protected boolean matchClassName(String className) {
                return declaringClass.equals(className);
            }
        });
        String[] scanPackages = StringUtils.toStringArray(basePackages);
        ObjectContainer.BASE_PACKAGES = scanPackages;
        scanner.scan(scanPackages);
    }
}
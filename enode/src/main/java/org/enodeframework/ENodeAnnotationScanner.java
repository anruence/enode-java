package org.enodeframework;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Event;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

public class ENodeAnnotationScanner extends ClassPathBeanDefinitionScanner {

    public ENodeAnnotationScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    protected void registerDefaultFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(Command.class));
        this.addIncludeFilter(new AnnotationTypeFilter(Event.class));
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        for (BeanDefinitionHolder holder : beanDefinitions) {
            GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
            this.getRegistry().registerBeanDefinition(holder.getBeanName(), definition);
        }
        return beanDefinitions;
    }
}

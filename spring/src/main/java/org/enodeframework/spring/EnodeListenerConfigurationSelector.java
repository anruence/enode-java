package org.enodeframework.spring;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

/**
 * A {@link DeferredImportSelector} implementation with the lowest order to import a
 * {@link EnodeBootstrapRegistrar} as late as possible.
 * {@link EnodeAutoConfiguration} as late as possible.
 *
 * @author anruence@gmail.com
 * @since 1.0.5
 */
@Order
public class EnodeListenerConfigurationSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{
                EnodeBootstrapRegistrar.class.getName(),
                EnodeAutoConfiguration.class.getName(),
                EnodeEventStoreAutoConfig.class.getName(),
                EnodeKafkaAutoConfiguration.class.getName(),
                EnodeRocketMQAutoConfig.class.getName()
        };
    }
}

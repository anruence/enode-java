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
public class EnodeConfigurationSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{
                EnodeBootstrapRegistrar.class.getName(),
                EnodeBeanContainerAutoConfig.class.getName(),
                EnodeAutoConfiguration.class.getName(),
                EnodeMemoryEventStoreAutoConfig.class.getName(),
                EnodeJDBCMySQLEventStoreAutoConfig.class.getName(),
                EnodeJDBCPgEventStoreAutoConfig.class.getName(),
                EnodePgEventStoreAutoConfig.class.getName(),
                EnodeMySQLEventStoreAutoConfig.class.getName(),
                EnodeMongoEventStoreAutoConfig.class.getName(),
                EnodeKafkaAutoConfiguration.class.getName(),
                EnodeOnsAutoConfig.class.getName(),
                EnodePulsarAutoConfig.class.getName(),
                EnodeRocketMQAutoConfig.class.getName(),
        };
    }
}

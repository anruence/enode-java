package org.enodeframework.spring;

import org.enodeframework.eventing.impl.InMemoryEventStore;
import org.enodeframework.eventing.impl.InMemoryPublishedVersionStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "memory", matchIfMissing = true)
public class EnodeMemoryEventStoreAutoConfig {

    @Bean
    public InMemoryEventStore inMemoryEventStore() {
        return new InMemoryEventStore();
    }

    @Bean
    public InMemoryPublishedVersionStore inMemoryPublishedVersionStore() {
        return new InMemoryPublishedVersionStore();
    }
}

package com.enode;

import com.enode.eventing.impl.InMemoryEventStore;
import com.enode.infrastructure.impl.inmemory.InMemoryPublishedVersionStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InMemoryConfiguration {

    @Bean
    public InMemoryEventStore eventStore() {
        return new InMemoryEventStore();
    }

    @Bean
    public InMemoryPublishedVersionStore publishedVersionStore() {
        return new InMemoryPublishedVersionStore();
    }
}

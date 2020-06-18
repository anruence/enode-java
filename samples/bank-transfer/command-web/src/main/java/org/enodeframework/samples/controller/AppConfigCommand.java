package org.enodeframework.samples.controller;

import org.enodeframework.eventing.impl.InMemoryEventStore;
import org.enodeframework.eventing.impl.InMemoryPublishedVersionStore;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigCommand {

    @Bean
    public DefaultCommandResultProcessor commandResultProcessor() {
        DefaultCommandResultProcessor processor = new DefaultCommandResultProcessor();
        return processor;
    }


    @Bean
    public InMemoryPublishedVersionStore versionStore() {
        return new InMemoryPublishedVersionStore();
    }

    @Bean
    public InMemoryEventStore eventStore() {
        return new InMemoryEventStore();
    }
}

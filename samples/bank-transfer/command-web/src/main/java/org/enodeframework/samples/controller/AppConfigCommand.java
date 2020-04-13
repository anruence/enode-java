package org.enodeframework.samples.controller;

import com.google.common.collect.Lists;
import org.enodeframework.ENodeBootstrap;
import org.enodeframework.eventing.impl.InMemoryEventStore;
import org.enodeframework.eventing.impl.InMemoryPublishedVersionStore;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigCommand {
    @Bean
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor();
        return processor;
    }

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("org.enodeframework.samples"));
        return bootstrap;
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

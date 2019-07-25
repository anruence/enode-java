package com.enodeframework.samples.controller;

import com.enodeframework.ENodeBootstrap;
import com.enodeframework.eventing.impl.InMemoryEventStore;
import com.enodeframework.infrastructure.impl.InMemoryPublishedVersionStore;
import com.enodeframework.queue.command.CommandResultProcessor;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigCommand {
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor();
        processor.setPort(6000);
        return processor;
    }

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("com.enodeframework.samples"));
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

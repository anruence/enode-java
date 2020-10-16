package org.enodeframework.test.config;

import org.enodeframework.test.mock.MockApplicationMessagePublisher;
import org.enodeframework.test.mock.MockDomainEventPublisher;
import org.enodeframework.test.mock.MockEventStore;
import org.enodeframework.test.mock.MockPublishableExceptionPublisher;
import org.enodeframework.test.mock.MockPublishedVersionStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class TestMockConfig {
    @Bean
    public MockPublishableExceptionPublisher mockPublishableExceptionPublisher() {
        return new MockPublishableExceptionPublisher();
    }

    @Bean
    public MockDomainEventPublisher mockDomainEventPublisher() {
        return new MockDomainEventPublisher();
    }

    @Bean
    public MockApplicationMessagePublisher mockApplicationMessagePublisher() {
        return new MockApplicationMessagePublisher();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mock")
    public MockEventStore mockEventStore() {
        return new MockEventStore();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mock")
    public MockPublishedVersionStore mockPublishedVersionStore() {
        return new MockPublishedVersionStore();
    }
}

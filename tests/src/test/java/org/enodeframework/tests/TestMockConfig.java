package org.enodeframework.tests;

import org.enodeframework.tests.mocks.MockApplicationMessagePublisher;
import org.enodeframework.tests.mocks.MockDomainEventPublisher;
import org.enodeframework.tests.mocks.MockEventStore;
import org.enodeframework.tests.mocks.MockPublishableExceptionPublisher;
import org.enodeframework.tests.mocks.MockPublishedVersionStore;
import org.springframework.context.annotation.Bean;

public class TestMockConfig {
    @Bean
    public MockPublishableExceptionPublisher mockPublishableExceptionPublisher() {
        return new MockPublishableExceptionPublisher();
    }

    @Bean
    public MockEventStore mockEventStore() {
        return new MockEventStore();
    }

    @Bean
    public MockPublishedVersionStore mockPublishedVersionStore() {
        return new MockPublishedVersionStore();
    }

    @Bean
    public MockDomainEventPublisher mockDomainEventPublisher() {
        return new MockDomainEventPublisher();
    }

    @Bean
    public MockApplicationMessagePublisher mockApplicationMessagePublisher() {
        return new MockApplicationMessagePublisher();
    }
}

package org.enodeframework.tests;

import org.enodeframework.tests.Mocks.MockApplicationMessagePublisher;
import org.enodeframework.tests.Mocks.MockDomainEventPublisher;
import org.enodeframework.tests.Mocks.MockEventStore;
import org.enodeframework.tests.Mocks.MockPublishableExceptionPublisher;
import org.enodeframework.tests.Mocks.MockPublishedVersionStore;
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

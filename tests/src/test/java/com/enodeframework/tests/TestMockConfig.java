package com.enodeframework.tests;

import com.enodeframework.tests.Mocks.MockApplicationMessagePublisher;
import com.enodeframework.tests.Mocks.MockDomainEventPublisher;
import com.enodeframework.tests.Mocks.MockEventStore;
import com.enodeframework.tests.Mocks.MockPublishableExceptionPublisher;
import com.enodeframework.tests.Mocks.MockPublishedVersionStore;
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

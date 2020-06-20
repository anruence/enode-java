package org.enodeframework.tests.config;

import org.enodeframework.tests.mocks.MockApplicationMessagePublisher;
import org.enodeframework.tests.mocks.MockDomainEventPublisher;
import org.enodeframework.tests.mocks.MockEventStore;
import org.enodeframework.tests.mocks.MockPublishableExceptionPublisher;
import org.enodeframework.tests.mocks.MockPublishedVersionStore;
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
}

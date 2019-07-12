package com.enodeframework.tests.TestClasses;

import com.enodeframework.ENodeAutoConfiguration;
import com.enodeframework.commanding.ICommandService;
import com.enodeframework.domain.IMemoryCache;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.eventing.IEventStore;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.infrastructure.IMessageProcessor;
import com.enodeframework.infrastructure.IMessagePublisher;
import com.enodeframework.infrastructure.IPublishableException;
import com.enodeframework.infrastructure.IPublishedVersionStore;
import com.enodeframework.infrastructure.ProcessingDomainEventStreamMessage;
import com.enodeframework.tests.EnodeExtensionConfig;
import com.enodeframework.tests.KafkaEventConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ENodeAutoConfiguration.class, KafkaEventConfig.class, EnodeExtensionConfig.class})
public abstract class AbstractTest {
    @Autowired
    protected ICommandService _commandService;

    @Autowired
    protected IMemoryCache _memoryCache;

    @Autowired
    protected IEventStore _eventStore;

    @Autowired
    protected IPublishedVersionStore _publishedVersionStore;

    @Autowired
    protected IMessagePublisher<DomainEventStreamMessage> _domainEventPublisher;

    @Autowired
    protected IMessagePublisher<IApplicationMessage> _applicationMessagePublisher;

    @Autowired
    protected IMessagePublisher<IPublishableException> _publishableExceptionPublisher;

    @Autowired
    protected IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> processor;

}

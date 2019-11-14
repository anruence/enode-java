package org.enodeframework.tests.TestClasses;

import org.enodeframework.ENodeAutoConfiguration;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.domain.IMemoryCache;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.IEventStore;
import org.enodeframework.eventing.IProcessingEventProcessor;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.tests.EnodeExtensionConfig;
import org.enodeframework.tests.KafkaEventConfig;
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
    protected IMessagePublisher<IDomainException> _publishableExceptionPublisher;
    @Autowired
    protected IProcessingEventProcessor processor;
}

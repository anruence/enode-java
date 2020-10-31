package org.enodeframework.test.mock;

import org.enodeframework.common.threading.ManualResetEvent;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.IEventProcessContext;

import java.util.List;

public class DomainEventStreamProcessContext implements IEventProcessContext {
    private DomainEventStreamMessage domainEventStreamMessage;
    private ManualResetEvent manualResetEvent;
    private List<Integer> versionList;

    public DomainEventStreamProcessContext(DomainEventStreamMessage domainEventStreamMessage, ManualResetEvent waitHandle, List<Integer> versionList) {
        this.domainEventStreamMessage = domainEventStreamMessage;
        manualResetEvent = waitHandle;
        this.versionList = versionList;
    }

    @Override
    public void notifyEventProcessed() {
        versionList.add(domainEventStreamMessage.getVersion());
        if (domainEventStreamMessage.getVersion() == 3) {
            manualResetEvent.set();
        }
    }
}
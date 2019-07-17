package com.enodeframework.tests.TestClasses;

import com.enodeframework.common.threading.ManualResetEvent;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.infrastructure.IMessageProcessContext;

import java.util.List;

public class DomainEventStreamProcessContext implements IMessageProcessContext {
    private DomainEventStreamMessage _domainEventStreamMessage;
    private ManualResetEvent _waitHandle;
    private List<Integer> _versionList;

    public DomainEventStreamProcessContext(DomainEventStreamMessage domainEventStreamMessage, ManualResetEvent waitHandle, List<Integer> versionList) {
        _domainEventStreamMessage = domainEventStreamMessage;
        _waitHandle = waitHandle;
        _versionList = versionList;
    }

    @Override
    public void notifyMessageProcessed() {
        _versionList.add(_domainEventStreamMessage.getVersion());
        if (_domainEventStreamMessage.getVersion() == 3) {
            _waitHandle.set();
        }
    }
}

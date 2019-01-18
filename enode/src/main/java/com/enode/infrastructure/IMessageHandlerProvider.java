package com.enode.infrastructure;

import java.util.List;

public interface IMessageHandlerProvider {
    List<MessageHandlerData<IMessageHandlerProxy1>> getHandlers(Class messageType);
}

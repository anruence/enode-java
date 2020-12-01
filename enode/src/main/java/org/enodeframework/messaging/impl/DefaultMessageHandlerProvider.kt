package org.enodeframework.messaging.impl

import org.enodeframework.eventing.IDomainEvent
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider
import org.enodeframework.messaging.IMessage
import org.enodeframework.messaging.IMessageHandlerProvider
import org.enodeframework.messaging.IMessageHandlerProxy1
import org.enodeframework.messaging.MessageHandlerData
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

/**
 * @author anruence@gmail.com
 */
class DefaultMessageHandlerProvider : AbstractHandlerProvider<Class<*>, IMessageHandlerProxy1, Class<*>>(), IMessageHandlerProvider {
    override fun getKey(method: Method): Class<*> {
        return method.parameterTypes[0]
    }

    override fun getHandlerProxyImplementationType(): Class<out IMessageHandlerProxy1> {
        return MessageHandlerProxy1::class.java
    }

    override fun isHandleMethodMatch(method: Method): Boolean {
        if (isSuspendMethod(method)) {
            return methodMatchSuspend(method)
        }
        return methodMatch(method)
    }

    private fun methodMatch(method: Method): Boolean {
        if (method.parameterTypes.size != 1) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[0]) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    private fun methodMatchSuspend(method: Method): Boolean {
        if (method.parameterTypes.size != 2) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[0]) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        if (Continuation::class.java != method.parameterTypes[1]) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    override fun isHandlerSourceMatchKey(handlerSource: Class<*>, key: Class<*>): Boolean {
        return key == handlerSource
    }

    override fun getHandlers(messageType: Class<*>): List<MessageHandlerData<IMessageHandlerProxy1>> {
        return getHandlersInternal(messageType)
    }
}
package org.enodeframework.messaging.impl

import org.enodeframework.eventing.IDomainEvent
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider
import org.enodeframework.infrastructure.impl.ManyType
import org.enodeframework.messaging.IMessage
import org.enodeframework.messaging.IMessageHandlerProxy2
import org.enodeframework.messaging.ITwoMessageHandlerProvider
import org.enodeframework.messaging.MessageHandlerData
import java.lang.reflect.Method
import java.util.*
import kotlin.coroutines.Continuation

/**
 * @author anruence@gmail.com
 */
class DefaultTwoMessageHandlerProvider : AbstractHandlerProvider<ManyType, IMessageHandlerProxy2, List<Class<*>>>(), ITwoMessageHandlerProvider {
    override fun getKey(method: Method): ManyType {
        return ManyType(listOf(*method.parameterTypes))
    }

    override fun getHandlerProxyImplementationType(): Class<out IMessageHandlerProxy2> {
        return MessageHandlerProxy2::class.java
    }

    override fun isHandlerSourceMatchKey(handlerSource: List<Class<*>>, key: ManyType): Boolean {
        for (type in key.types) {
            if (!handlerSource.stream().anyMatch { x: Class<*> -> x == type }) {
                return false
            }
        }
        return true
    }

    override fun isHandleMethodMatch(method: Method): Boolean {
        if (isSuspendMethod(method)) {
            return methodMatchSuspend(method)
        }
        return methodMatch(method)
    }

    private fun methodMatch(method: Method): Boolean {
        if (method.parameterTypes.size != 2) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[0]) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[1]) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    private fun methodMatchSuspend(method: Method): Boolean {
        if (method.parameterTypes.size != 3) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[0]) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[1]) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        if (Continuation::class.java != method.parameterTypes[2]) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    override fun getHandlers(messageTypes: List<Class<*>>): List<MessageHandlerData<IMessageHandlerProxy2>> {
        return getHandlersInternal(messageTypes)
    }
}
package org.enodeframework.messaging.impl

import org.enodeframework.eventing.IDomainEvent
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider
import org.enodeframework.infrastructure.impl.ManyType
import org.enodeframework.messaging.IMessage
import org.enodeframework.messaging.IMessageHandlerProxy3
import org.enodeframework.messaging.IThreeMessageHandlerProvider
import org.enodeframework.messaging.MessageHandlerData
import java.lang.reflect.Method
import java.util.*
import kotlin.coroutines.Continuation

/**
 * @author anruence@gmail.com
 */
class DefaultThreeMessageHandlerProvider : AbstractHandlerProvider<ManyType, IMessageHandlerProxy3, List<Class<*>>>(), IThreeMessageHandlerProvider {

    override fun getKey(method: Method): ManyType {
        return ManyType(Arrays.asList(*method.parameterTypes))
    }

    override fun getHandlerProxyImplementationType(): Class<out IMessageHandlerProxy3> {
        return MessageHandlerProxy3::class.java
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
        if (method.parameterTypes.size != 3) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[0]) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[1]) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[2]) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[2])) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    private fun methodMatchSuspend(method: Method): Boolean {
        if (method.parameterTypes.size != 4) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[0]) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[1]) {
            return false
        }
        if (IMessage::class.java == method.parameterTypes[2]) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[0])) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        if (!IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[2])) {
            return false
        }
        if (Continuation::class.java != method.parameterTypes[3]) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    override fun getHandlers(messageTypes: List<Class<*>>): List<MessageHandlerData<IMessageHandlerProxy3>> {
        return getHandlersInternal(messageTypes)
    }
}
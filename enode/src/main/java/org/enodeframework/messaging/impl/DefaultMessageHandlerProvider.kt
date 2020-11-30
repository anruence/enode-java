package org.enodeframework.messaging.impl

import org.enodeframework.common.container.IObjectContainer
import org.enodeframework.common.container.ObjectContainer
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider
import org.enodeframework.messaging.IMessage
import org.enodeframework.messaging.IMessageHandlerProvider
import org.enodeframework.messaging.IMessageHandlerProxy1
import java.lang.reflect.Method
import kotlin.reflect.jvm.kotlinFunction

/**
 * @author anruence@gmail.com
 */
class DefaultMessageHandlerProvider : AbstractHandlerProvider<Class<*>?, IMessageHandlerProxy1?, Class<*>?>(), IMessageHandlerProvider {
    override fun getKey(method: Method): Class<*> {
        return method.parameterTypes[0]
    }

    override fun getHandlerProxyImplementationType(): Class<out IMessageHandlerProxy1> {
        return MessageHandlerProxy1::class.java
    }

    override fun isHandleMethodMatch(method: Method): Boolean {
        val paramCount = method.parameterTypes.size
        if (paramCount != 1) {
            if (!isSuspendMethod(method)) {
                return false
            }
        }
        if (IMessage::class.java == method.parameterTypes[0]) {
            return false
        }
        return if (!IMessage::class.java.isAssignableFrom(method.parameterTypes[0])) {
            false
        } else isMethodAnnotationSubscribe(method)
    }

    override fun isSuspendMethod(method: Method): Boolean {
        return method.kotlinFunction?.isSuspend == true
    }

    override fun getObjectContainer(): IObjectContainer {
        return ObjectContainer.INSTANCE
    }

    override fun isHandlerSourceMatchKey(handlerSource: Class<*>?, key: Class<*>?): Boolean {
        return key == handlerSource
    }
}
package org.enodeframework.commanding.impl

import org.enodeframework.commanding.ICommand
import org.enodeframework.commanding.ICommandContext
import org.enodeframework.commanding.ICommandHandlerProvider
import org.enodeframework.commanding.ICommandHandlerProxy
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider
import org.enodeframework.messaging.MessageHandlerData
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

/**
 * @author anruence@gmail.com
 */
class DefaultCommandHandlerProvider : AbstractHandlerProvider<Class<*>, ICommandHandlerProxy, Class<*>>(), ICommandHandlerProvider {
    override fun getKey(method: Method): Class<*> {
        return method.parameterTypes[1]
    }

    override fun getHandlerProxyImplementationType(): Class<out ICommandHandlerProxy> {
        return CommandHandlerProxy::class.java
    }

    override fun isHandlerSourceMatchKey(handlerSource: Class<*>, key: Class<*>): Boolean {
        return key == handlerSource
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
        if (ICommandContext::class.java != method.parameterTypes[0]) {
            return false
        }
        if (ICommand::class.java == method.parameterTypes[1]) {
            return false
        }
        if (!ICommand::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    private fun methodMatchSuspend(method: Method): Boolean {
        if (method.parameterTypes.size != 3) {
            return false
        }
        if (ICommandContext::class.java != method.parameterTypes[0]) {
            return false
        }
        if (ICommand::class.java == method.parameterTypes[1]) {
            return false
        }
        if (!ICommand::class.java.isAssignableFrom(method.parameterTypes[1])) {
            return false
        }
        if (Continuation::class.java != method.parameterTypes[2]) {
            return false
        }
        return isMethodAnnotationSubscribe(method)
    }

    override fun getHandlers(commandType: Class<*>): List<MessageHandlerData<ICommandHandlerProxy>> {
        return getHandlersInternal(commandType)
    }
}
package org.enodeframework.commanding.impl

import org.enodeframework.commanding.ICommand
import org.enodeframework.commanding.ICommandContext
import org.enodeframework.commanding.ICommandHandlerProxy
import org.enodeframework.common.container.ObjectContainer
import java.lang.invoke.MethodHandle
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class CommandHandlerProxy : ICommandHandlerProxy {
    private lateinit var handlerType: Class<*>
    private var commandHandler: Any? = null
    private lateinit var methodHandle: MethodHandle
    private lateinit var method: Method
    override fun handleAsync(context: ICommandContext, command: ICommand): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        try {
            val result = methodHandle.invoke(getInnerObject(), context, command)
            if (result is CompletableFuture<*>) {
                result.thenAccept { future.complete(true) }
                return future
            }
            future.complete(true)
        } catch (throwable: Throwable) {
            future.completeExceptionally(throwable)
        }
        return future
    }

    override fun getInnerObject(): Any {
        if (commandHandler == null) {
            this.commandHandler = ObjectContainer.INSTANCE.resolve(handlerType)
        }
        return this.commandHandler!!;
    }

    override fun setHandlerType(handlerType: Class<*>) {
        this.handlerType = handlerType
    }

    override fun setMethodHandle(methodHandle: MethodHandle) {
        this.methodHandle = methodHandle
    }

    override fun getMethod(): Method {
        return method
    }

    override fun setMethod(method: Method) {
        this.method = method
    }
}
package org.enodeframework.messaging.impl

import org.enodeframework.messaging.IMessage
import org.enodeframework.messaging.IMessageHandlerProxy1
import java.lang.invoke.MethodHandle
import java.lang.reflect.Method
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.reflect.jvm.kotlinFunction

/**
 * @author anruence@gmail.com
 */
class MessageHandlerProxy1 : IMessageHandlerProxy1 {
    private lateinit var innerObject: Any
    private lateinit var methodHandle: MethodHandle
    private lateinit var method: Method

    override suspend fun handleAsync(message: IMessage) {
        if (method.kotlinFunction?.isSuspend == true) {
            invokeSuspend(getInnerObject(), message)
            return
        }
        methodHandle.invoke(getInnerObject(), message)
    }

    private suspend fun invokeSuspend(obj: Any, message: IMessage): Any? = suspendCoroutineUninterceptedOrReturn { continuation ->
        methodHandle.invoke(obj, message, continuation)
    }

    override fun getInnerObject(): Any {
        return innerObject
    }

    override fun setInnerObject(innerObject: Any) {
        this.innerObject = innerObject
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
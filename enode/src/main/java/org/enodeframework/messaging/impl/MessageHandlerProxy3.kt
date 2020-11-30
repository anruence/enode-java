package org.enodeframework.messaging.impl

import org.enodeframework.messaging.IMessage
import org.enodeframework.messaging.IMessageHandlerProxy3
import java.lang.invoke.MethodHandle
import java.lang.reflect.Method
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.reflect.jvm.kotlinFunction

/**
 * @author anruence@gmail.com
 */
class MessageHandlerProxy3 : IMessageHandlerProxy3 {
    private lateinit var innerObject: Any
    private lateinit var methodHandle: MethodHandle
    private lateinit var method: Method

    override suspend fun handleAsync(message1: IMessage, message2: IMessage, message3: IMessage) {
        //参数按照方法定义参数类型列表传递
        if (method.kotlinFunction?.isSuspend == true) {
            invokeSuspend(innerObject, message1, message2, message3)
            return
        }
        methodHandle.invoke(innerObject, message1, message2, message3)
    }

    private suspend fun invokeSuspend(obj: Any, message1: IMessage, message2: IMessage, message3: IMessage): Any? = suspendCoroutineUninterceptedOrReturn { continuation ->
        methodHandle.invoke(obj, message1, message2, message3, continuation)
    }

    override fun setInnerObject(innerObject: Any) {
        this.innerObject = innerObject
    }

    override fun getInnerObject(): Any {
        return innerObject
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
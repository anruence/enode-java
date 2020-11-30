package org.enodeframework.infrastructure

import java.lang.invoke.MethodHandle
import java.lang.reflect.Method

interface MethodInvocation {
    fun setMethodHandle(methodHandle: MethodHandle)
    fun setMethod(method: Method)
    fun getMethod(): Method
}
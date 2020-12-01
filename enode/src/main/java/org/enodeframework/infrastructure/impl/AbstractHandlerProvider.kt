package org.enodeframework.infrastructure.impl

import org.enodeframework.annotation.Command
import org.enodeframework.annotation.Event
import org.enodeframework.annotation.Priority
import org.enodeframework.annotation.Subscribe
import org.enodeframework.common.container.IObjectContainer
import org.enodeframework.common.container.ObjectContainer
import org.enodeframework.common.exception.HandlerRegisterException
import org.enodeframework.infrastructure.IAssemblyInitializer
import org.enodeframework.infrastructure.IObjectProxy
import org.enodeframework.infrastructure.MethodInvocation
import org.enodeframework.messaging.MessageHandlerData
import org.reflections.ReflectionUtils
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.jvm.kotlinFunction
import kotlin.streams.toList

abstract class AbstractHandlerProvider<TKey, THandlerProxyInterface, THandlerSource> : IAssemblyInitializer where THandlerProxyInterface : IObjectProxy, THandlerProxyInterface : MethodInvocation {
    private val handlerDict: MutableMap<TKey, MutableList<THandlerProxyInterface>> = HashMap()
    private val messageHandlerDict: MutableMap<TKey, MessageHandlerData<THandlerProxyInterface>> = HashMap()
    private val lookup = MethodHandles.lookup()

    protected abstract fun getKey(method: Method): TKey
    protected abstract fun getHandlerProxyImplementationType(): Class<out THandlerProxyInterface>
    protected abstract fun isHandlerSourceMatchKey(handlerSource: THandlerSource, key: TKey): Boolean

    /**
     * kotlin suspend 方法会多一个参数 kotlin.coroutines.Continuation
     */
    protected abstract fun isHandleMethodMatch(method: Method): Boolean

    /**
     * 是否是一个可挂起的方法
     */
    protected open fun isSuspendMethod(method: Method): Boolean {
        return method.kotlinFunction?.isSuspend == true
    }

    protected open val objectContainer: IObjectContainer
        get() = ObjectContainer.INSTANCE

    override fun initialize(componentTypes: Set<Class<*>>) {
        componentTypes.stream().filter { type: Class<*> -> isHandlerType(type) }.forEach { handlerType: Class<*> -> registerHandler(handlerType) }
        initializeHandlerPriority()
    }

    fun getHandlersInternal(source: THandlerSource): List<MessageHandlerData<THandlerProxyInterface>> {
        val handlerDataList: MutableList<MessageHandlerData<THandlerProxyInterface>> = ArrayList()
        messageHandlerDict.keys.stream()
                .filter { key: TKey -> isHandlerSourceMatchKey(source, key) }
                .forEach { key: TKey -> handlerDataList.add(messageHandlerDict[key]!!) }
        return handlerDataList
    }

    private fun initializeHandlerPriority() {
        handlerDict.forEach { (key: TKey, handlers: List<THandlerProxyInterface>) ->
            val handlerData = MessageHandlerData<THandlerProxyInterface>()
            val listHandlers: MutableList<THandlerProxyInterface> = ArrayList()
            val queueHandlerDict: MutableMap<THandlerProxyInterface, Int> = HashMap()
            handlers.forEach(Consumer { handler: THandlerProxyInterface ->
                val priority = getHandleMethodPriority(handler)
                if (priority == 0) {
                    listHandlers.add(handler)
                } else {
                    queueHandlerDict[handler] = priority
                }
            })
            handlerData.allHandlers = handlers
            handlerData.listHandlers = listHandlers
            handlerData.queuedHandlers = queueHandlerDict.entries.stream()
                    .sorted(Comparator.comparingInt { v -> v.value })
                    .map { x -> x.key }.toList()
            messageHandlerDict[key] = handlerData
        }
    }

    private fun getHandleMethodPriority(handler: THandlerProxyInterface): Int {
        val method = handler.getMethod()
        var priority = 0
        val methodPriority = method.getAnnotation(Priority::class.java)
        if (methodPriority != null) {
            priority = methodPriority.value
        }
        if (priority == 0) {
            val classPriority = handler.getInnerObject().javaClass.getAnnotation(Priority::class.java)
            if (classPriority != null) {
                priority = classPriority.value
            }
        }
        return priority
    }

    private fun isHandlerType(type: Class<*>): Boolean {
        if (type.isInterface) {
            return false
        }
        return if (Modifier.isAbstract(type.modifiers)) {
            false
        } else containsHandleType(type)
    }

    private fun containsHandleType(type: Class<*>): Boolean {
        return type.isAnnotationPresent(Command::class.java) || type.isAnnotationPresent(Event::class.java)
    }

    protected fun isMethodAnnotationSubscribe(method: Method): Boolean {
        return method.isAnnotationPresent(Subscribe::class.java)
    }

    private fun registerHandler(handlerType: Class<*>) {
        val handleMethods = ReflectionUtils.getMethods(handlerType, { method: Method -> isHandleMethodMatch(method) })
        handleMethods.forEach { method: Method ->
            try {
                // 反射Method转换为MethodHandle，提高效率
                val handleMethod = lookup.findVirtual(handlerType, method.name, MethodType.methodType(method.returnType, method.parameterTypes))
                val key = getKey(method)
                val handlers = handlerDict.computeIfAbsent(key, { ArrayList() })
                val handlerProxy = objectContainer.resolve(getHandlerProxyImplementationType())
                handlerProxy.setInnerObject(objectContainer.resolve(handlerType))
                handlerProxy.setMethod(method)
                handlerProxy.setMethodHandle(handleMethod)
                handlers.add(handlerProxy)
            } catch (e: Exception) {
                throw HandlerRegisterException(e)
            }
        }
    }
}
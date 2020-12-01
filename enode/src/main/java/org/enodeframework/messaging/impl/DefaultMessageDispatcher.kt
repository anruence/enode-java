package org.enodeframework.messaging.impl

import com.google.common.collect.Lists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import org.enodeframework.common.function.Action4
import org.enodeframework.common.io.IOHelper

import org.enodeframework.common.io.Task
import org.enodeframework.infrastructure.IObjectProxy
import org.enodeframework.infrastructure.ITypeNameProvider
import org.enodeframework.messaging.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * @author anruence@gmail.com
 */
class DefaultMessageDispatcher(private val typeNameProvider: ITypeNameProvider, private val messageHandlerProvider: IMessageHandlerProvider, private val twoMessageHandlerProvider: ITwoMessageHandlerProvider, private val threeMessageHandlerProvider: IThreeMessageHandlerProvider) : IMessageDispatcher {
    override fun dispatchMessageAsync(message: IMessage): CompletableFuture<Boolean> {
        return dispatchMessages(Lists.newArrayList(message))
    }

    override fun dispatchMessagesAsync(messages: List<IMessage>): CompletableFuture<Boolean> {
        return dispatchMessages(messages)
    }

    private fun dispatchMessages(messages: List<IMessage>): CompletableFuture<Boolean> {
        val messageCount = messages.size
        if (messageCount == 0) {
            return Task.completedTask
        }
        val rootDispatching = RootDispatching()
        //先对每个事件调用其Handler
        val queueMessageDispatching = QueueMessageDispatching(this, rootDispatching, messages)
        dispatchSingleMessage(queueMessageDispatching.dequeueMessage(), queueMessageDispatching)
        //如果有至少两个事件，则尝试调用针对两个事件的Handler
        if (messageCount >= 2) {
            val twoMessageHandlers = twoMessageHandlerProvider.getHandlers(messages.stream().map { x: IMessage -> x.javaClass }.collect(Collectors.toList()) as List<Class<*>>)
            if (twoMessageHandlers.isNotEmpty()) {
                dispatchMultiMessage(messages, twoMessageHandlers, rootDispatching) { multiMessageDispatching: MultiMessageDispatching, handlerProxy: IMessageHandlerProxy2, queueHandler: QueuedHandler<IMessageHandlerProxy2>?, retryTimes: Int -> dispatchTwoMessageToHandlerAsync(multiMessageDispatching, handlerProxy, queueHandler, retryTimes) }
            }
        }
        //如果有至少三个事件，则尝试调用针对三个事件的Handler
        if (messageCount >= 3) {
            val threeMessageHandlers = threeMessageHandlerProvider.getHandlers(messages.stream().map { x: IMessage -> x.javaClass }.collect(Collectors.toList()) as List<Class<*>>)
            if (threeMessageHandlers.isNotEmpty()) {
                dispatchMultiMessage(messages, threeMessageHandlers, rootDispatching) { multiMessageDispatching: MultiMessageDispatching, handlerProxy: IMessageHandlerProxy3, queueHandler: QueuedHandler<IMessageHandlerProxy3>?, retryTimes: Int -> dispatchThreeMessageToHandlerAsync(multiMessageDispatching, handlerProxy, queueHandler, retryTimes) }
            }
        }
        return rootDispatching.taskCompletionSource
    }

    fun dispatchSingleMessage(message: IMessage, queueMessageDispatching: QueueMessageDispatching) {
        val messageHandlerDataList = messageHandlerProvider.getHandlers(message.javaClass)
        if (messageHandlerDataList.isEmpty()) {
            queueMessageDispatching.onMessageHandled(message)
            return
        }
        messageHandlerDataList.forEach(Consumer { messageHandlerData: MessageHandlerData<IMessageHandlerProxy1> ->
            val singleMessageDispatching = SingleMessageDispatching(message, queueMessageDispatching, messageHandlerData.allHandlers, typeNameProvider)
            if (messageHandlerData.listHandlers != null && messageHandlerData.listHandlers.isNotEmpty()) {
                messageHandlerData.listHandlers.forEach(Consumer { handler: IMessageHandlerProxy1 -> dispatchSingleMessageToHandlerAsync(singleMessageDispatching, handler, null, 0) })
            }
            if (messageHandlerData.queuedHandlers != null && messageHandlerData.queuedHandlers.isNotEmpty()) {
                val queueHandler = QueuedHandler(messageHandlerData.queuedHandlers) { queuedHandler: QueuedHandler<IMessageHandlerProxy1>?, nextHandler: IMessageHandlerProxy1 -> dispatchSingleMessageToHandlerAsync(singleMessageDispatching, nextHandler, queuedHandler, 0) }
                dispatchSingleMessageToHandlerAsync(singleMessageDispatching, queueHandler.dequeueHandler(), queueHandler, 0)
            }
        })
    }

    private fun <T : IObjectProxy?> dispatchMultiMessage(messages: List<IMessage>, messageHandlerDataList: List<MessageHandlerData<T>>, rootDispatching: RootDispatching, dispatchAction: Action4<MultiMessageDispatching, T, QueuedHandler<T>?, Int>) {
        messageHandlerDataList.forEach(Consumer { messageHandlerData: MessageHandlerData<T> ->
            val multiMessageDispatching = MultiMessageDispatching(messages, messageHandlerData.allHandlers, rootDispatching, typeNameProvider)
            if (messageHandlerData.listHandlers != null && messageHandlerData.listHandlers.isNotEmpty()) {
                messageHandlerData.listHandlers.forEach(Consumer { handler: T -> dispatchAction.apply(multiMessageDispatching, handler, null, 0) })
            }
            if (messageHandlerData.queuedHandlers != null && messageHandlerData.queuedHandlers.isNotEmpty()) {
                val queuedHandler = QueuedHandler(messageHandlerData.queuedHandlers
                ) { currentQueuedHandler: QueuedHandler<T>?, nextHandler: T -> dispatchAction.apply(multiMessageDispatching, nextHandler, currentQueuedHandler, 0) }
                dispatchAction.apply(multiMessageDispatching, queuedHandler.dequeueHandler(), queuedHandler, 0)
            }
        })
    }

    private fun dispatchSingleMessageToHandlerAsync(singleMessageDispatching: SingleMessageDispatching, handlerProxy: IMessageHandlerProxy1, queueHandler: QueuedHandler<IMessageHandlerProxy1>?, retryTimes: Int) {
        val message = singleMessageDispatching.message
        val messageTypeName = typeNameProvider.getTypeName(message.javaClass)
        val handlerType: Class<*> = handlerProxy.getInnerObject().javaClass
        val handlerTypeName = typeNameProvider.getTypeName(handlerType)
        handleSingleMessageAsync(singleMessageDispatching, handlerProxy, handlerTypeName, messageTypeName, queueHandler, retryTimes)
    }

    private fun dispatchTwoMessageToHandlerAsync(multiMessageDispatching: MultiMessageDispatching, handlerProxy: IMessageHandlerProxy2, queueHandler: QueuedHandler<IMessageHandlerProxy2>?, retryTimes: Int) {
        val handlerType: Class<*> = handlerProxy.getInnerObject().javaClass
        val handlerTypeName = typeNameProvider.getTypeName(handlerType)
        handleTwoMessageAsync(multiMessageDispatching, handlerProxy, handlerTypeName, queueHandler, 0)
    }

    private fun dispatchThreeMessageToHandlerAsync(multiMessageDispatching: MultiMessageDispatching, handlerProxy: IMessageHandlerProxy3, queueHandler: QueuedHandler<IMessageHandlerProxy3>?, retryTimes: Int) {
        val handlerType: Class<*> = handlerProxy.getInnerObject().javaClass
        val handlerTypeName = typeNameProvider.getTypeName(handlerType)
        handleThreeMessageAsync(multiMessageDispatching, handlerProxy, handlerTypeName, queueHandler, 0)
    }

    private fun handleSingleMessageAsync(singleMessageDispatching: SingleMessageDispatching, handlerProxy: IMessageHandlerProxy1, handlerTypeName: String, messageTypeName: String, queueHandler: QueuedHandler<IMessageHandlerProxy1>?, retryTimes: Int) {
        val message = singleMessageDispatching.message
        IOHelper.tryAsyncActionRecursivelyWithoutResult("HandleSingleMessageAsync", {
            CoroutineScope(Dispatchers.Default).async {
                handlerProxy.handleAsync(message)
            }.asCompletableFuture()
        }, {
            singleMessageDispatching.removeHandledHandler(handlerTypeName)
            queueHandler?.onHandlerFinished(handlerProxy)
            if (logger.isDebugEnabled) {
                logger.debug("Message handled success, handlerType:{}, messageType:{}, messageId:{}", handlerTypeName, message.javaClass.name, message.id)
            }
        }, { String.format("[messageId:%s, messageType:%s, handlerType:%s]", message.id, message.javaClass.name, handlerProxy.getInnerObject().javaClass.name) },
                null, retryTimes, true)
    }

    private fun handleTwoMessageAsync(multiMessageDispatching: MultiMessageDispatching, handlerProxy: IMessageHandlerProxy2, handlerTypeName: String, queueHandler: QueuedHandler<IMessageHandlerProxy2>?, retryTimes: Int) {
        val messages = multiMessageDispatching.messages
        val message1 = messages[0]
        val message2 = messages[1]
        IOHelper.tryAsyncActionRecursively("HandleTwoMessageAsync", {
            CoroutineScope(Dispatchers.Default).async {
                handlerProxy.handleAsync(message1, message2)
            }.asCompletableFuture()
        }, {
            multiMessageDispatching.removeHandledHandler(handlerTypeName)
            queueHandler?.onHandlerFinished(handlerProxy)
            if (logger.isDebugEnabled) {
                logger.debug("TwoMessage handled success, [messages:{}], handlerType:{}]", java.lang.String.join("|", Arrays.stream(messages).map { x: IMessage -> String.format("id:%s,type:%s", x.id, x.javaClass.name) }.collect(Collectors.toList())), handlerTypeName)
            }
        }, {
            String.format("[messages:%s, handlerType:%s]", java.lang.String.join("|", Arrays.stream(messages).map { x: IMessage -> String.format("id:%s,type:%s", x.id, x.javaClass.name) }.collect(Collectors.toList())), handlerProxy.getInnerObject().javaClass.name)
        }, null, retryTimes, true)
    }

    private fun handleThreeMessageAsync(
            multiMessageDispatching: MultiMessageDispatching, handlerProxy: IMessageHandlerProxy3, handlerTypeName: String,
            queueHandler: QueuedHandler<IMessageHandlerProxy3>?, retryTimes: Int) {
        val messages = multiMessageDispatching.messages
        val message1 = messages[0]
        val message2 = messages[1]
        val message3 = messages[2]
        IOHelper.tryAsyncActionRecursively("HandleThreeMessageAsync", {
            CoroutineScope(Dispatchers.Default).async {
                handlerProxy.handleAsync(message1, message2, message3)
            }.asCompletableFuture()
        }, {
            multiMessageDispatching.removeHandledHandler(handlerTypeName)
            queueHandler?.onHandlerFinished(handlerProxy)
            if (logger.isDebugEnabled) {
                logger.debug("ThreeMessage handled success, [messages:{}, handlerType:{}]", Arrays.stream(messages).map { x: IMessage -> String.format("id:%s,type:%s", x.id, x.javaClass.name) }.collect(Collectors.joining("|")), handlerTypeName)
            }
        }, { String.format("[messages:%s, handlerType:%s]", Arrays.stream(messages).map { x: IMessage -> String.format("id:%s,type:%s", x.id, x.javaClass.name) }.collect(Collectors.joining("|")), handlerProxy.getInnerObject().javaClass.name) },
                null, retryTimes, true)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultMessageDispatcher::class.java)
    }
}
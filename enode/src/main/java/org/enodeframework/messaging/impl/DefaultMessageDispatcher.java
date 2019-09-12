package org.enodeframework.messaging.impl;

import com.google.common.collect.Lists;
import org.enodeframework.common.function.Action2;
import org.enodeframework.common.function.Action4;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.messaging.IMessage;
import org.enodeframework.messaging.IMessageDispatcher;
import org.enodeframework.messaging.IMessageHandlerProvider;
import org.enodeframework.messaging.IMessageHandlerProxy1;
import org.enodeframework.messaging.IMessageHandlerProxy2;
import org.enodeframework.messaging.IMessageHandlerProxy3;
import org.enodeframework.messaging.IThreeMessageHandlerProvider;
import org.enodeframework.messaging.ITwoMessageHandlerProvider;
import org.enodeframework.messaging.MessageHandlerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DefaultMessageDispatcher implements IMessageDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageDispatcher.class);
    @Autowired
    private ITypeNameProvider typeNameProvider;
    @Autowired
    private IMessageHandlerProvider handlerProvider;
    @Autowired
    private ITwoMessageHandlerProvider twoMessageHandlerProvider;
    @Autowired
    private IThreeMessageHandlerProvider threeMessageHandlerProvider;

    public DefaultMessageDispatcher setTypeNameProvider(ITypeNameProvider typeNameProvider) {
        this.typeNameProvider = typeNameProvider;
        return this;
    }

    public DefaultMessageDispatcher setHandlerProvider(IMessageHandlerProvider handlerProvider) {
        this.handlerProvider = handlerProvider;
        return this;
    }

    public DefaultMessageDispatcher setTwoMessageHandlerProvider(ITwoMessageHandlerProvider twoMessageHandlerProvider) {
        this.twoMessageHandlerProvider = twoMessageHandlerProvider;
        return this;
    }

    public DefaultMessageDispatcher setThreeMessageHandlerProvider(IThreeMessageHandlerProvider threeMessageHandlerProvider) {
        this.threeMessageHandlerProvider = threeMessageHandlerProvider;
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> dispatchMessageAsync(IMessage message) {
        return dispatchMessages(Lists.newArrayList(message));
    }

    @Override
    public CompletableFuture<AsyncTaskResult> dispatchMessagesAsync(List<? extends IMessage> messages) {
        return dispatchMessages(messages);
    }

    private CompletableFuture<AsyncTaskResult> dispatchMessages(List<? extends IMessage> messages) {
        int messageCount = messages.size();
        if (messageCount == 0) {
            return CompletableFuture.completedFuture(AsyncTaskResult.Success);
        }
        RootDispatching rootDispatching = new RootDispatching();
        //先对每个事件调用其Handler
        QueueMessageDispatching queueMessageDispatching = new QueueMessageDispatching(this, rootDispatching, messages);
        dispatchSingleMessage(queueMessageDispatching.dequeueMessage(), queueMessageDispatching);
        //如果有至少两个事件，则尝试调用针对两个事件的Handler
        if (messageCount >= 2) {
            List<MessageHandlerData<IMessageHandlerProxy2>> twoMessageHandlers = twoMessageHandlerProvider.getHandlers(messages.stream().map(x -> x.getClass()).collect(Collectors.toList()));
            if (!twoMessageHandlers.isEmpty()) {
                dispatchMultiMessage(messages, twoMessageHandlers, rootDispatching, this::dispatchTwoMessageToHandlerAsync);
            }
        }
        //如果有至少三个事件，则尝试调用针对三个事件的Handler
        if (messageCount >= 3) {
            List<MessageHandlerData<IMessageHandlerProxy3>> threeMessageHandlers = threeMessageHandlerProvider.getHandlers(messages.stream().map(x -> x.getClass()).collect(Collectors.toList()));
            if (!threeMessageHandlers.isEmpty()) {
                dispatchMultiMessage(messages, threeMessageHandlers, rootDispatching, this::dispatchThreeMessageToHandlerAsync);
            }
        }
        return rootDispatching.getTaskCompletionSource();
    }

    private void dispatchSingleMessage(IMessage message, QueueMessageDispatching queueMessageDispatching) {
        List<MessageHandlerData<IMessageHandlerProxy1>> messageHandlerDataList = handlerProvider.getHandlers(message.getClass());
        if (messageHandlerDataList.isEmpty()) {
            queueMessageDispatching.onMessageHandled(message);
            return;
        }
        messageHandlerDataList.forEach(messageHandlerData -> {
            SingleMessageDispatching singleMessageDispatching = new SingleMessageDispatching(message, queueMessageDispatching, messageHandlerData.allHandlers, typeNameProvider);
            if (messageHandlerData.listHandlers != null && !messageHandlerData.listHandlers.isEmpty()) {
                messageHandlerData.listHandlers.forEach(handler -> dispatchSingleMessageToHandlerAsync(singleMessageDispatching, handler, null, 0));
            }
            if (messageHandlerData.queuedHandlers != null && !messageHandlerData.queuedHandlers.isEmpty()) {
                QueuedHandler<IMessageHandlerProxy1> queueHandler = new QueuedHandler<>(messageHandlerData.queuedHandlers, (queuedHandler, nextHandler) -> dispatchSingleMessageToHandlerAsync(singleMessageDispatching, nextHandler, queuedHandler, 0));
                dispatchSingleMessageToHandlerAsync(singleMessageDispatching, queueHandler.dequeueHandler(), queueHandler, 0);
            }
        });
    }

    private <T extends IObjectProxy> void dispatchMultiMessage(List<? extends IMessage> messages, List<MessageHandlerData<T>> messageHandlerDataList, RootDispatching rootDispatching, Action4<MultiMessageDisptaching, T, QueuedHandler<T>, Integer> dispatchAction) {
        messageHandlerDataList.forEach(messageHandlerData -> {
            MultiMessageDisptaching multiMessageDispatching = new MultiMessageDisptaching(messages, messageHandlerData.allHandlers, rootDispatching, typeNameProvider);
            if (messageHandlerData.listHandlers != null && !messageHandlerData.listHandlers.isEmpty()) {
                messageHandlerData.listHandlers.forEach(handler -> {
                    dispatchAction.apply(multiMessageDispatching, handler, null, 0);
                });
            }
            if (messageHandlerData.queuedHandlers != null && !messageHandlerData.queuedHandlers.isEmpty()) {
                QueuedHandler<T> queuedHandler = new QueuedHandler<>(messageHandlerData.queuedHandlers, (currentQueuedHandler, nextHandler) ->
                        dispatchAction.apply(multiMessageDispatching, nextHandler, currentQueuedHandler, 0)
                );
                dispatchAction.apply(multiMessageDispatching, queuedHandler.dequeueHandler(), queuedHandler, 0);
            }
        });
    }

    private void dispatchSingleMessageToHandlerAsync(SingleMessageDispatching singleMessageDispatching, IMessageHandlerProxy1 handlerProxy, QueuedHandler<IMessageHandlerProxy1> queueHandler, int retryTimes) {
        IMessage message = singleMessageDispatching.getMessage();
        String messageTypeName = typeNameProvider.getTypeName(message.getClass());
        Class handlerType = handlerProxy.getInnerObject().getClass();
        String handlerTypeName = typeNameProvider.getTypeName(handlerType);
        handleSingleMessageAsync(singleMessageDispatching, handlerProxy, handlerTypeName, messageTypeName, queueHandler, retryTimes);
    }

    private void dispatchTwoMessageToHandlerAsync(MultiMessageDisptaching multiMessageDispatching, IMessageHandlerProxy2 handlerProxy, QueuedHandler<IMessageHandlerProxy2> queueHandler, int retryTimes) {
        Class handlerType = handlerProxy.getInnerObject().getClass();
        String handlerTypeName = typeNameProvider.getTypeName(handlerType);
        handleTwoMessageAsync(multiMessageDispatching, handlerProxy, handlerTypeName, queueHandler, 0);
    }

    private void dispatchThreeMessageToHandlerAsync(MultiMessageDisptaching multiMessageDispatching, IMessageHandlerProxy3 handlerProxy, QueuedHandler<IMessageHandlerProxy3> queueHandler, int retryTimes) {
        Class handlerType = handlerProxy.getInnerObject().getClass();
        String handlerTypeName = typeNameProvider.getTypeName(handlerType);
        handleThreeMessageAsync(multiMessageDispatching, handlerProxy, handlerTypeName, queueHandler, 0);
    }

    private void handleSingleMessageAsync(SingleMessageDispatching singleMessageDispatching, IMessageHandlerProxy1 handlerProxy, String handlerTypeName, String messageTypeName, QueuedHandler<IMessageHandlerProxy1> queueHandler, int retryTimes) {
        IMessage message = singleMessageDispatching.getMessage();
        IOHelper.tryAsyncActionRecursively("HandleSingleMessageAsync",
                () -> handlerProxy.handleAsync(message),
                result -> {
                    singleMessageDispatching.removeHandledHandler(handlerTypeName);
                    if (queueHandler != null) {
                        queueHandler.onHandlerFinished(handlerProxy);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Message handled success, handlerType:{}, messageType:{}, messageId:{}", handlerTypeName, message.getClass().getName(), message.getId());
                    }
                },
                () -> String.format("[messageId:%s, messageType:%s, handlerType:%s]", message.getId(), message.getClass().getName(), handlerProxy.getInnerObject().getClass().getName()),
                errorMessage -> logger.error("Handle single message has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    private void handleTwoMessageAsync(MultiMessageDisptaching multiMessageDispatching, IMessageHandlerProxy2 handlerProxy, String handlerTypeName, QueuedHandler<IMessageHandlerProxy2> queueHandler, int retryTimes) {
        IMessage[] messages = multiMessageDispatching.getMessages();
        IMessage message1 = messages[0];
        IMessage message2 = messages[1];
        IOHelper.tryAsyncActionRecursively("HandleTwoMessageAsync",
                () -> handlerProxy.handleAsync(message1, message2),
                result ->
                {
                    multiMessageDispatching.removeHandledHandler(handlerTypeName);
                    if (queueHandler != null) {
                        queueHandler.onHandlerFinished(handlerProxy);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("TwoMessage handled success, [messages:{}], handlerType:{}]", String.join("|", Arrays.stream(messages).map(x -> String.format("id:%s,type:%s", x.getId(), x.getClass().getName())).collect(Collectors.toList())), handlerTypeName);
                    }
                },
                () -> String.format("[messages:%s, handlerType:%s]", String.join("|", Arrays.stream(messages).map(x -> String.format("id:%s,type:%s", x.getId(), x.getClass().getName())).collect(Collectors.toList())), handlerProxy.getInnerObject().getClass().getName()),
                errorMessage -> logger.error("Handle two message has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    private void handleThreeMessageAsync(
            MultiMessageDisptaching multiMessageDispatching, IMessageHandlerProxy3 handlerProxy, String handlerTypeName,
            QueuedHandler<IMessageHandlerProxy3> queueHandler, int retryTimes) {
        IMessage[] messages = multiMessageDispatching.getMessages();
        IMessage message1 = messages[0];
        IMessage message2 = messages[1];
        IMessage message3 = messages[2];
        IOHelper.tryAsyncActionRecursively("HandleThreeMessageAsync",
                () -> handlerProxy.handleAsync(message1, message2, message3),
                result ->
                {
                    multiMessageDispatching.removeHandledHandler(handlerTypeName);
                    if (queueHandler != null) {
                        queueHandler.onHandlerFinished(handlerProxy);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("ThreeMessage handled success, [messages:{}, handlerType:{}]", String.join("|", Arrays.stream(messages).map(x -> String.format("id:%s,type:%s", x.getId(), x.getClass().getName())).collect(Collectors.toList())), handlerTypeName);
                    }
                },
                () -> String.format("[messages:%s, handlerType:%s]", String.join("|", Arrays.stream(messages).map(x -> String.format("id:%s,type:%s", x.getId(), x.getClass().getName())).collect(Collectors.toList())), handlerProxy.getInnerObject().getClass().getName()),
                errorMessage -> logger.error("Handle three message has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    class RootDispatching {
        private CompletableFuture<AsyncTaskResult> taskCompletionSource;
        private ConcurrentMap<Object, Boolean> childDispatchingDict;

        public RootDispatching() {
            taskCompletionSource = new CompletableFuture<>();
            childDispatchingDict = new ConcurrentHashMap<>();
        }

        public CompletableFuture<AsyncTaskResult> getTaskCompletionSource() {
            return taskCompletionSource;
        }

        public void addChildDispatching(Object childDispatching) {
            childDispatchingDict.put(childDispatching, false);
        }

        public void onChildDispatchingFinished(Object childDispatching) {
            if (childDispatchingDict.remove(childDispatching) != null) {
                if (childDispatchingDict.isEmpty()) {
                    taskCompletionSource.complete(AsyncTaskResult.Success);
                }
            }
        }
    }

    class QueueMessageDispatching {
        private DefaultMessageDispatcher dispatcher;
        private RootDispatching rootDispatching;
        private ConcurrentLinkedQueue<IMessage> messageQueue;

        public QueueMessageDispatching(DefaultMessageDispatcher dispatcher, RootDispatching rootDispatching, List<? extends IMessage> messages) {
            this.dispatcher = dispatcher;
            messageQueue = new ConcurrentLinkedQueue<>();
            messageQueue.addAll(messages);
            this.rootDispatching = rootDispatching;
            this.rootDispatching.addChildDispatching(this);
        }

        public IMessage dequeueMessage() {
            return messageQueue.poll();
        }

        public void onMessageHandled(IMessage message) {
            IMessage nextMessage = dequeueMessage();
            if (nextMessage == null) {
                rootDispatching.onChildDispatchingFinished(this);
                return;
            }
            dispatcher.dispatchSingleMessage(nextMessage, this);
        }
    }

    class MultiMessageDisptaching {
        private IMessage[] messages;
        private ConcurrentMap<String, IObjectProxy> handlerDict;
        private RootDispatching rootDispatching;

        public MultiMessageDisptaching(List<? extends IMessage> messages, List<? extends IObjectProxy> handlers, RootDispatching rootDispatching, ITypeNameProvider typeNameProvider) {
            this.messages = messages.toArray(new IMessage[0]);
            handlerDict = new ConcurrentHashMap<>();
            handlers.forEach(x -> handlerDict.putIfAbsent(typeNameProvider.getTypeName(x.getInnerObject().getClass()), x));
            this.rootDispatching = rootDispatching;
            this.rootDispatching.addChildDispatching(this);
        }

        public IMessage[] getMessages() {
            return messages;
        }

        public void removeHandledHandler(String handlerTypeName) {
            if (handlerDict.remove(handlerTypeName) != null) {
                if (handlerDict.isEmpty()) {
                    rootDispatching.onChildDispatchingFinished(this);
                }
            }
        }
    }

    class SingleMessageDispatching {
        private ConcurrentMap<String, IObjectProxy> handlerDict;
        private QueueMessageDispatching queueMessageDispatching;
        private IMessage message;

        public SingleMessageDispatching(IMessage message, QueueMessageDispatching queueMessageDispatching, List<? extends IObjectProxy> handlers, ITypeNameProvider typeNameProvider) {
            this.message = message;
            this.queueMessageDispatching = queueMessageDispatching;
            this.handlerDict = new ConcurrentHashMap<>();
            handlers.forEach(x -> handlerDict.putIfAbsent(typeNameProvider.getTypeName(x.getInnerObject().getClass()), x));
        }

        public void removeHandledHandler(String handlerTypeName) {
            if (handlerDict.remove(handlerTypeName) != null) {
                if (handlerDict.isEmpty()) {
                    queueMessageDispatching.onMessageHandled(message);
                }
            }
        }

        public IMessage getMessage() {
            return message;
        }
    }

    class QueuedHandler<T extends IObjectProxy> {
        private Action2<QueuedHandler<T>, T> dispatchToNextHandler;
        private ConcurrentLinkedQueue<T> handlerQueue;

        public QueuedHandler(List<T> handlers, Action2<QueuedHandler<T>, T> dispatchToNextHandler) {
            handlerQueue = new ConcurrentLinkedQueue<>();
            handlerQueue.addAll(handlers);
            this.dispatchToNextHandler = dispatchToNextHandler;
        }

        public T dequeueHandler() {
            return handlerQueue.poll();
        }

        public void onHandlerFinished(T handler) {
            T nextHandler = dequeueHandler();
            if (nextHandler != null) {
                dispatchToNextHandler.apply(this, nextHandler);
            }
        }
    }
}

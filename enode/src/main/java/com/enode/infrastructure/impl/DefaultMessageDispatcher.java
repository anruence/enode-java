package com.enode.infrastructure.impl;

import com.google.common.collect.Lists;
import com.enode.common.function.Action2;
import com.enode.common.function.Action4;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.IOHelper;
import com.enode.common.logging.ENodeLogger;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessageDispatcher;
import com.enode.infrastructure.IMessageHandlerProvider;
import com.enode.infrastructure.IMessageHandlerProxy1;
import com.enode.infrastructure.IMessageHandlerProxy2;
import com.enode.infrastructure.IMessageHandlerProxy3;
import com.enode.infrastructure.IObjectProxy;
import com.enode.infrastructure.IThreeMessageHandlerProvider;
import com.enode.infrastructure.ITwoMessageHandlerProvider;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.MessageHandlerData;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class DefaultMessageDispatcher implements IMessageDispatcher {
    private static final Logger _logger = ENodeLogger.getLog();

    private final ITypeNameProvider _typeNameProvider;
    private final IMessageHandlerProvider _handlerProvider;
    private final ITwoMessageHandlerProvider _twoMessageHandlerProvider;
    private final IThreeMessageHandlerProvider _threeMessageHandlerProvider;
    private final IOHelper _ioHelper;

    @Inject
    public DefaultMessageDispatcher(
            ITypeNameProvider typeNameProvider,
            IMessageHandlerProvider handlerProvider,
            ITwoMessageHandlerProvider twoMessageHandlerProvider,
            IThreeMessageHandlerProvider threeMessageHandlerProvider,
            IOHelper ioHelper) {
        _typeNameProvider = typeNameProvider;
        _handlerProvider = handlerProvider;
        _twoMessageHandlerProvider = twoMessageHandlerProvider;
        _threeMessageHandlerProvider = threeMessageHandlerProvider;
        _ioHelper = ioHelper;
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
        RootDisptaching rootDispatching = new RootDisptaching();

        //先对每个事件调用其Handler
        QueueMessageDispatching queueMessageDispatching = new QueueMessageDispatching(this, rootDispatching, messages);
        dispatchSingleMessage(queueMessageDispatching.dequeueMessage(), queueMessageDispatching);

        //如果有至少两个事件，则尝试调用针对两个事件的Handler
        if (messageCount >= 2) {
            List<MessageHandlerData<IMessageHandlerProxy2>> twoMessageHandlers = _twoMessageHandlerProvider.getHandlers(messages.stream().map(x -> x.getClass()).collect(Collectors.toList()));
            if (!twoMessageHandlers.isEmpty()) {
                dispatchMultiMessage(messages, twoMessageHandlers, rootDispatching, this::dispatchTwoMessageToHandlerAsync);
            }
        }
        //如果有至少三个事件，则尝试调用针对三个事件的Handler
        if (messageCount >= 3) {
            List<MessageHandlerData<IMessageHandlerProxy3>> threeMessageHandlers = _threeMessageHandlerProvider.getHandlers(messages.stream().map(x -> x.getClass()).collect(Collectors.toList()));
            if (!threeMessageHandlers.isEmpty()) {
                dispatchMultiMessage(messages, threeMessageHandlers, rootDispatching, this::dispatchThreeMessageToHandlerAsync);
            }
        }
        return rootDispatching.getTaskCompletionSource();
    }

    private void dispatchSingleMessage(IMessage message, QueueMessageDispatching queueMessageDispatching) {
        List<MessageHandlerData<IMessageHandlerProxy1>> messageHandlerDataList = _handlerProvider.getHandlers(message.getClass());
        if (messageHandlerDataList.isEmpty()) {
            queueMessageDispatching.onMessageHandled(message);
            return;
        }

        messageHandlerDataList.forEach(messageHandlerData -> {
            SingleMessageDispatching singleMessageDispatching = new SingleMessageDispatching(message, queueMessageDispatching, messageHandlerData.AllHandlers, _typeNameProvider);
            if (messageHandlerData.ListHandlers != null && !messageHandlerData.ListHandlers.isEmpty()) {
                messageHandlerData.ListHandlers.forEach(handler -> dispatchSingleMessageToHandlerAsync(singleMessageDispatching, handler, null, 0));
            }

            if (messageHandlerData.QueuedHandlers != null && !messageHandlerData.QueuedHandlers.isEmpty()) {
                QueuedHandler<IMessageHandlerProxy1> queueHandler = new QueuedHandler<>(messageHandlerData.QueuedHandlers, (queuedHandler, nextHandler) -> dispatchSingleMessageToHandlerAsync(singleMessageDispatching, nextHandler, queuedHandler, 0));

                dispatchSingleMessageToHandlerAsync(singleMessageDispatching, queueHandler.dequeueHandler(), queueHandler, 0);
            }
        });
    }

    private <T extends IObjectProxy> void dispatchMultiMessage(List<? extends IMessage> messages, List<MessageHandlerData<T>> messageHandlerDataList,
                                                               RootDisptaching rootDispatching, Action4<MultiMessageDisptaching, T, QueuedHandler<T>, Integer> dispatchAction) {
        messageHandlerDataList.forEach(messageHandlerData -> {
            MultiMessageDisptaching multiMessageDispatching = new MultiMessageDisptaching(messages, messageHandlerData.AllHandlers, rootDispatching, _typeNameProvider);

            if (messageHandlerData.ListHandlers != null && !messageHandlerData.ListHandlers.isEmpty()) {
                messageHandlerData.ListHandlers.forEach(handler -> {
                    try {
                        dispatchAction.apply(multiMessageDispatching, handler, null, 0);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            if (messageHandlerData.QueuedHandlers != null && !messageHandlerData.QueuedHandlers.isEmpty()) {
                QueuedHandler<T> queuedHandler = new QueuedHandler<>(messageHandlerData.QueuedHandlers, (currentQueuedHandler, nextHandler) ->
                        dispatchAction.apply(multiMessageDispatching, nextHandler, currentQueuedHandler, 0)
                );

                try {
                    dispatchAction.apply(multiMessageDispatching, queuedHandler.dequeueHandler(), queuedHandler, 0);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void dispatchSingleMessageToHandlerAsync(SingleMessageDispatching singleMessageDispatching, IMessageHandlerProxy1 handlerProxy,
                                                     QueuedHandler<IMessageHandlerProxy1> queueHandler, int retryTimes) {
        IMessage message = singleMessageDispatching.getMessage();
        String messageTypeName = _typeNameProvider.getTypeName(message.getClass());
        Class handlerType = handlerProxy.getInnerObject().getClass();
        String handlerTypeName = _typeNameProvider.getTypeName(handlerType);

        handleSingleMessageAsync(singleMessageDispatching, handlerProxy, handlerTypeName, messageTypeName, queueHandler, retryTimes);
    }

    private void dispatchTwoMessageToHandlerAsync(MultiMessageDisptaching multiMessageDispatching, IMessageHandlerProxy2 handlerProxy, QueuedHandler<IMessageHandlerProxy2> queueHandler, int retryTimes) {
        Class handlerType = handlerProxy.getInnerObject().getClass();
        String handlerTypeName = _typeNameProvider.getTypeName(handlerType);
        handleTwoMessageAsync(multiMessageDispatching, handlerProxy, handlerTypeName, queueHandler, 0);
    }

    private void dispatchThreeMessageToHandlerAsync(MultiMessageDisptaching multiMessageDispatching, IMessageHandlerProxy3 handlerProxy, QueuedHandler<IMessageHandlerProxy3> queueHandler, int retryTimes) {
        Class handlerType = handlerProxy.getInnerObject().getClass();
        String handlerTypeName = _typeNameProvider.getTypeName(handlerType);
        handleThreeMessageAsync(multiMessageDispatching, handlerProxy, handlerTypeName, queueHandler, 0);
    }

    private void handleSingleMessageAsync(
            SingleMessageDispatching singleMessageDispatching, IMessageHandlerProxy1 handlerProxy,
            String handlerTypeName, String messageTypeName, QueuedHandler<IMessageHandlerProxy1> queueHandler,
            int retryTimes) {
        IMessage message = singleMessageDispatching.getMessage();

        _ioHelper.tryAsyncActionRecursively("HandleSingleMessageAsync",
                () -> handlerProxy.handleAsync(message),
                currentRetryTimes -> handleSingleMessageAsync(singleMessageDispatching, handlerProxy, handlerTypeName, messageTypeName, queueHandler, currentRetryTimes),
                result ->
                {
                    singleMessageDispatching.removeHandledHandler(handlerTypeName);
                    if (queueHandler != null) {
                        queueHandler.onHandlerFinished(handlerProxy);
                    }
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Message handled success, handlerType:{}, messageType:{}, messageId:{}", handlerTypeName, message.getClass().getName(), message.id());
                    }
                },
                () -> String.format("[messageId:%s, messageType:%s, handlerType:%s]", message.id(), message.getClass().getName(), handlerProxy.getInnerObject().getClass().getName()),
                errorMessage ->
                        _logger.error(String.format("Handle single message has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage))
                ,
                retryTimes, true);
    }

    private void handleTwoMessageAsync(
            MultiMessageDisptaching multiMessageDispatching, IMessageHandlerProxy2 handlerProxy,
            String handlerTypeName, QueuedHandler<IMessageHandlerProxy2> queueHandler, int retryTimes) {
        IMessage[] messages = multiMessageDispatching.getMessages();
        IMessage message1 = messages[0];
        IMessage message2 = messages[1];

        _ioHelper.tryAsyncActionRecursively("HandleTwoMessageAsync",
                () -> handlerProxy.handleAsync(message1, message2),
                currentRetryTimes -> handleTwoMessageAsync(multiMessageDispatching, handlerProxy, handlerTypeName, queueHandler, currentRetryTimes),
                result ->
                {
                    multiMessageDispatching.removeHandledHandler(handlerTypeName);
                    if (queueHandler != null) {
                        queueHandler.onHandlerFinished(handlerProxy);
                    }
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("TwoMessage handled success, [messages:{}], handlerType:{}]", String.join("|", Arrays.stream(messages).map(x -> String.format("id:%s,type:%s", x.id(), x.getClass().getName())).collect(Collectors.toList())), handlerTypeName);
                    }
                },
                () -> String.format("[messages:%s, handlerType:%s]", String.join("|", Arrays.stream(messages).map(x -> String.format("id:%s,type:%s", x.id(), x.getClass().getName())).collect(Collectors.toList())), handlerProxy.getInnerObject().getClass().getName()),
                errorMessage ->
                        _logger.error(String.format("Handle two message has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage))
                ,
                retryTimes, true);
    }

    private void handleThreeMessageAsync(
            MultiMessageDisptaching multiMessageDispatching, IMessageHandlerProxy3 handlerProxy, String handlerTypeName,
            QueuedHandler<IMessageHandlerProxy3> queueHandler, int retryTimes) {
        IMessage[] messages = multiMessageDispatching.getMessages();
        IMessage message1 = messages[0];
        IMessage message2 = messages[1];
        IMessage message3 = messages[2];

        _ioHelper.tryAsyncActionRecursively("HandleThreeMessageAsync",
                () -> handlerProxy.handleAsync(message1, message2, message3),
                currentRetryTimes -> handleThreeMessageAsync(multiMessageDispatching, handlerProxy, handlerTypeName, queueHandler, currentRetryTimes),

                result ->
                {
                    multiMessageDispatching.removeHandledHandler(handlerTypeName);
                    if (queueHandler != null) {
                        queueHandler.onHandlerFinished(handlerProxy);
                    }

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("ThreeMessage handled success, [messages:{}, handlerType:{}]", String.join("|", Arrays.stream(messages).map(x -> String.format("id:%s,type:%s", x.id(), x.getClass().getName())).collect(Collectors.toList())), handlerTypeName);
                    }
                },
                () -> String.format("[messages:%s, handlerType:{1}]", String.join("|", Arrays.stream(messages).map(x -> String.format("id:%s,type:%s", x.id(), x.getClass().getName())).collect(Collectors.toList())), handlerProxy.getInnerObject().getClass().getName()),
                errorMessage -> _logger.error(String.format("Handle three message has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }

    class RootDisptaching {
        private CompletableFuture<AsyncTaskResult> _taskCompletionSource;
        private ConcurrentMap<Object, Boolean> _childDispatchingDict;

        public CompletableFuture<AsyncTaskResult> getTaskCompletionSource() {
            return _taskCompletionSource;
        }

        public RootDisptaching() {
            _taskCompletionSource = new CompletableFuture<>();
            _childDispatchingDict = new ConcurrentHashMap<>();
        }

        public void addChildDispatching(Object childDispatching) {
            _childDispatchingDict.put(childDispatching, false);
        }

        public void onChildDispatchingFinished(Object childDispatching) {
            if (_childDispatchingDict.remove(childDispatching) != null) {
                if (_childDispatchingDict.isEmpty()) {
                    _taskCompletionSource.complete(AsyncTaskResult.Success);
                }
            }
        }
    }

    class QueueMessageDispatching {
        private DefaultMessageDispatcher _dispatcher;
        private RootDisptaching _rootDispatching;
        private ConcurrentLinkedQueue<IMessage> _messageQueue;

        public QueueMessageDispatching(DefaultMessageDispatcher dispatcher, RootDisptaching rootDispatching, List<? extends IMessage> messages) {
            _dispatcher = dispatcher;
            _messageQueue = new ConcurrentLinkedQueue<>();

            //TODO _messageQueue
            messages.forEach(message ->
                    _messageQueue.add(message)
            );
            _rootDispatching = rootDispatching;
            _rootDispatching.addChildDispatching(this);
        }

        public IMessage dequeueMessage() {
            return _messageQueue.poll();
        }

        public void onMessageHandled(IMessage message) {
            IMessage nextMessage = dequeueMessage();
            if (nextMessage == null) {
                _rootDispatching.onChildDispatchingFinished(this);
                return;
            }
            _dispatcher.dispatchSingleMessage(nextMessage, this);
        }
    }

    class MultiMessageDisptaching {
        private IMessage[] _messages;
        private ConcurrentMap<String, IObjectProxy> _handlerDict;
        private RootDisptaching _rootDispatching;

        public IMessage[] getMessages() {
            return _messages;
        }

        public MultiMessageDisptaching(List<? extends IMessage> messages, List<? extends IObjectProxy> handlers, RootDisptaching rootDispatching, ITypeNameProvider typeNameProvider) {
            _messages = messages.toArray(new IMessage[0]);
            _handlerDict = new ConcurrentHashMap<>();
            handlers.forEach(x -> _handlerDict.putIfAbsent(typeNameProvider.getTypeName(x.getInnerObject().getClass()), x));
            _rootDispatching = rootDispatching;
            _rootDispatching.addChildDispatching(this);
        }

        public void removeHandledHandler(String handlerTypeName) {
            if (_handlerDict.remove(handlerTypeName) != null) {
                if (_handlerDict.isEmpty()) {
                    _rootDispatching.onChildDispatchingFinished(this);
                }
            }
        }
    }

    class SingleMessageDispatching {
        private ConcurrentMap<String, IObjectProxy> _handlerDict;
        private QueueMessageDispatching _queueMessageDispatching;

        private IMessage message;

        public SingleMessageDispatching(IMessage message, QueueMessageDispatching queueMessageDispatching, List<? extends IObjectProxy> handlers, ITypeNameProvider typeNameProvider) {
            this.message = message;
            _queueMessageDispatching = queueMessageDispatching;
            _handlerDict = new ConcurrentHashMap<>();
            handlers.forEach(x -> _handlerDict.putIfAbsent(typeNameProvider.getTypeName(x.getInnerObject().getClass()), x));
        }

        public void removeHandledHandler(String handlerTypeName) {
            if (_handlerDict.remove(handlerTypeName) != null) {
                if (_handlerDict.isEmpty()) {
                    _queueMessageDispatching.onMessageHandled(message);
                }
            }
        }

        public IMessage getMessage() {
            return message;
        }
    }

    class QueuedHandler<T extends IObjectProxy> {
        private Action2<QueuedHandler<T>, T> _dispatchToNextHandler;
        private ConcurrentLinkedQueue<T> _handlerQueue;

        public QueuedHandler(List<T> handlers, Action2<QueuedHandler<T>, T> dispatchToNextHandler) {
            _handlerQueue = new ConcurrentLinkedQueue<>();
            handlers.forEach(handler -> _handlerQueue.add(handler));
            _dispatchToNextHandler = dispatchToNextHandler;
        }

        public T dequeueHandler() {
            return _handlerQueue.poll();
        }

        public void onHandlerFinished(T handler) {
            T nextHandler = dequeueHandler();
            if (nextHandler != null) {
                try {
                    _dispatchToNextHandler.apply(this, nextHandler);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

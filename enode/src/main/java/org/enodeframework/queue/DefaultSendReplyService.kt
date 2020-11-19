package org.enodeframework.queue

import com.google.common.cache.CacheBuilder
import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetSocket
import io.vertx.core.net.SocketAddress
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameParser
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.common.io.ReplySocketAddress
import org.enodeframework.common.io.Task
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.common.utilities.InetUtil
import org.enodeframework.common.utilities.ReplyMessage
import org.enodeframework.queue.domainevent.DomainEventHandledMessage
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class DefaultSendReplyService(private val serializeService: ISerializeService) : CoroutineVerticle(), ISendReplyService {
    private var started = false
    private var stoped = false
    private lateinit var netClient: NetClient
    private val netSocketCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10)
            .build<String, Promise<NetSocket>>()

    override suspend fun start() {
        if (!started) {
            netClient = vertx.createNetClient()
            started = true
        }
    }

    override suspend fun stop() {
        if (!stoped) {
            netClient.close()
            stoped = true
        }
    }

    override fun sendCommandReply(commandResult: CommandResult, replyAddress: ReplySocketAddress): CompletableFuture<Boolean> {
        val replyMessage = ReplyMessage()
        replyMessage.code = CommandReturnType.CommandExecuted.value
        replyMessage.commandResult = commandResult
        return sendReply(replyMessage, replyAddress)
    }

    override fun sendEventReply(eventHandledMessage: DomainEventHandledMessage, replyAddress: ReplySocketAddress): CompletableFuture<Boolean> {
        val replyMessage = ReplyMessage()
        replyMessage.code = CommandReturnType.EventHandled.value
        replyMessage.eventHandledMessage = eventHandledMessage
        return sendReply(replyMessage, replyAddress)
    }

    private fun sendReply(replyMessage: ReplyMessage?, replySocketAddress: ReplySocketAddress): CompletableFuture<Boolean> {
        val socketAddress = SocketAddress.inetSocketAddress(replySocketAddress.port, replySocketAddress.host)
        val message = serializeService.serialize(replyMessage)
        val address = InetUtil.toUri(replySocketAddress)
        val replyAddress = String.format("%s.%s", "client", address)
        val promise = netSocketCache.get(address) {
            val value = Promise.promise<NetSocket>()
            netClient.connect(socketAddress, value)
            value
        }
        promise.future().onFailure { throwable: Throwable? ->
            netSocketCache.invalidate(address)
            logger.error("connect occurs unexpected error, msg: {}", message, throwable)
        }.onSuccess { socket: NetSocket ->
            socket.exceptionHandler { throwable: Throwable? ->
                netSocketCache.invalidate(address)
                socket.close()
                logger.error("socket occurs unexpected error, msg: {}", message, throwable)
            }
            socket.closeHandler {
                netSocketCache.invalidate(address)
                logger.error("socket closed, indicatedServerName: {},writeHandlerID: {}", socket.indicatedServerName(), socket.writeHandlerID())
            }
            socket.handler(FrameParser { parse: AsyncResult<JsonObject?> ->
                if (parse.succeeded()) {
                    logger.info("receive server response: {}, {}", message, parse)
                }
            })
            socket.endHandler { netSocketCache.invalidate(address) }
            FrameHelper.sendFrame("send", address, replyAddress, JsonObject(message), socket)
        }
        return Task.completedTask
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultSendReplyService::class.java)
    }
}
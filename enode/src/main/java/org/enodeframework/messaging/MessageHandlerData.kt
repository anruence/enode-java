package org.enodeframework.messaging

import org.enodeframework.infrastructure.IObjectProxy
import java.util.*

/**
 * @author anruence@gmail.com
 */
class MessageHandlerData<T : IObjectProxy?> {
    var allHandlers: List<T> = ArrayList()
    var listHandlers: List<T> = ArrayList()
    var queuedHandlers: List<T> = ArrayList()
}
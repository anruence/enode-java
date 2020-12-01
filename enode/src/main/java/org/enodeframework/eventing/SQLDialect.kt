package org.enodeframework.eventing

/**
 * @author anruence@gmail.com
 */
interface SQLDialect {
    /**
     * 解析SQL错误信息中的重复id
     */
    fun getDuplicatedId(throwable: Throwable): String
}
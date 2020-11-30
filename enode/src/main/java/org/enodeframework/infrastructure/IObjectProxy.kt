package org.enodeframework.infrastructure

interface IObjectProxy {
    fun getInnerObject(): Any
    fun setInnerObject(innerObject: Any)
}
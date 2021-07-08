package org.enodeframework.infrastructure.impl

import org.enodeframework.common.exception.EnodeClassNotFoundException
import org.enodeframework.infrastructure.ITypeNameProvider

/**
 * @author anruence@gmail.com
 */
class DefaultTypeNameProvider(private val typeDict: Map<String, String>) : ITypeNameProvider {
    override fun getTypeName(type: Class<*>): String {
        return type.name
    }

    override fun getType(typeName: String): Class<*> {
        return try {
            Class.forName(typeName)
        } catch (e: ClassNotFoundException) {
            val type = typeDict[typeName]
            if (type != null) {
                return getType(type)
            }
            throw EnodeClassNotFoundException(e)
        }
    }

    /**
     * Check whether the given name points back to the given alias as an alias
     * in the other direction already, catching a circular reference upfront
     * and throwing a corresponding IllegalStateException.
     *
     * @param name  the candidate name
     * @param alias the candidate alias
     */
    fun checkForAliasCircle(name: String, alias: String) {
        check(!hasAlias(name, alias)) {
            "Cannot register alias '" + alias +
                    "' for name '" + name + "': Circular reference - '" +
                    name + "' is a direct or indirect alias for '" + alias + "' already"
        }
    }

    /**
     * Determine whether the given name has the given alias registered.
     *
     * @param name  the name to check
     * @param alias the alias to look for
     */
    private fun hasAlias(name: String, alias: String): Boolean {
        val registeredName = typeDict[alias]
        return registeredName == name || (registeredName != null && hasAlias(name, registeredName))
    }

    init {
        typeDict.forEach { (name: String, alias: String) -> checkForAliasCircle(name, alias) }
    }
}
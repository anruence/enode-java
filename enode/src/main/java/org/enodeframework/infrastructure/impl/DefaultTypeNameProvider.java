package org.enodeframework.infrastructure.impl;

import org.enodeframework.common.exception.EnodeClassNotFoundException;
import org.enodeframework.infrastructure.ITypeNameProvider;

import java.util.Map;
import java.util.Objects;

/**
 * @author anruence@gmail.com
 */
public class DefaultTypeNameProvider implements ITypeNameProvider {

    private final Map<String, String> typeDict;

    public DefaultTypeNameProvider(Map<String, String> typeDict) {
        this.typeDict = typeDict;
        typeDict.forEach(this::checkForAliasCircle);
    }

    @Override
    public String getTypeName(Class<?> type) {
        return type.getName();
    }

    @Override
    public Class<?> getType(String typeName) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            String type = typeDict.get(typeName);
            if (type != null) {
                return getType(type);
            }
            throw new EnodeClassNotFoundException(e);
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
    public void checkForAliasCircle(String name, String alias) {
        if (hasAlias(name, alias)) {
            throw new IllegalStateException("Cannot register alias '" + alias +
                    "' for name '" + name + "': Circular reference - '" +
                    name + "' is a direct or indirect alias for '" + alias + "' already");
        }
    }

    /**
     * Determine whether the given name has the given alias registered.
     *
     * @param name  the name to check
     * @param alias the alias to look for
     */
    private boolean hasAlias(String name, String alias) {
        String registeredName = this.typeDict.get(alias);
        return Objects.equals(registeredName, name) || (registeredName != null
                && hasAlias(name, registeredName));
    }
}

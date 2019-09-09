package org.enodeframework.infrastructure.impl;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class ManyType {
    private List<Class> types;

    public ManyType(List<Class> types) {
        if (new HashSet<>(types).size() != types.size()) {
            throw new IllegalArgumentException("Invalid ManyType:" + String.join("|", types.stream().map(Class::getName).collect(Collectors.toList())));
        }
        this.types = types;
    }

    public List<Class> getTypes() {
        return types;
    }

    @Override
    public int hashCode() {
        return types.stream().map(Object::hashCode).reduce((x, y) -> x ^ y).get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ManyType)) {
            return false;
        }
        ManyType other = (ManyType) obj;
        if (this.types.size() != other.types.size()) {
            return false;
        }
        return types.stream().allMatch(type -> other.types.stream().anyMatch(x -> x == type))
                && other.types.stream().allMatch(type -> types.stream().anyMatch(x -> x == type));
    }
}

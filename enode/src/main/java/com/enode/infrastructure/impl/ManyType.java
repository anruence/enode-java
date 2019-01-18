package com.enode.infrastructure.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ManyType {
    private List<Class> _types = new ArrayList<>();

    public ManyType(List<Class> types) {
        if(new HashSet<>(types).size() != types.size()){
            throw new IllegalArgumentException("Invalid ManyType:" + String.join("|", types.stream().map(x -> x.getName()).collect(Collectors.toList())));
        }

        _types = types;
    }

    public List<Class> getTypes() {
        return _types;
    }

    @Override
    public int hashCode() {
        return _types.stream().map(x -> x.hashCode()).reduce((x, y) -> x ^ y).get();
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

        if (this._types.size() != other._types.size()) {
            return false;
        }

        return _types.stream().allMatch(type -> other._types.stream().anyMatch(x -> x == type))
                && other._types.stream().allMatch(type -> _types.stream().anyMatch(x -> x == type));
    }
}

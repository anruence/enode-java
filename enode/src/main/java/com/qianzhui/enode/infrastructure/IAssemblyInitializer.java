package com.qianzhui.enode.infrastructure;

import java.util.Set;

public interface IAssemblyInitializer {
    void initialize(Set<Class<?>> componentTypes);
}

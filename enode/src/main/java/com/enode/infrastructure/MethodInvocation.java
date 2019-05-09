package com.enode.infrastructure;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

public interface MethodInvocation {

    Method getMethod();

    void setMethod(Method method);

    void setHandlerType(Class handlerType);

    void setMethodHandle(MethodHandle methodHandle);
}

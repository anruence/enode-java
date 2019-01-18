package com.enode.common.thirdparty.guice;

import com.enode.common.container.LifeStyle;
import com.google.inject.Scope;
import com.google.inject.Scopes;

public class ScopeConverter {
    public static Scope toGuiceScope(LifeStyle life) {
        if (life == null) {
            return Scopes.SINGLETON;
        }
        switch (life) {
            case Transient:
                return Scopes.NO_SCOPE;
            default:
                return Scopes.SINGLETON;
        }
    }
}

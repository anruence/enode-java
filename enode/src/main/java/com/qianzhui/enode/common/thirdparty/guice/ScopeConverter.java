package com.qianzhui.enode.common.thirdparty.guice;

import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.qianzhui.enode.common.container.LifeStyle;

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

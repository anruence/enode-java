package com.enode.infrastructure;

import com.enode.common.container.LifeStyle;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    LifeStyle life() default LifeStyle.Singleton;
}

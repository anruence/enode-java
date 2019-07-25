package com.enodeframework.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author anruence@gmail.com
 * event execute priority
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
    int value() default 0;
}

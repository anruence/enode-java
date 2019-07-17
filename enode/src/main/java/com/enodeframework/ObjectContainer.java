package com.enodeframework;

import com.enodeframework.common.container.IObjectContainer;

import java.util.Map;

/**
 * Spring Ioc容器
 *
 * @author anruence@gmail.com
 */
public class ObjectContainer {

    public static IObjectContainer container;

    public static <T> Map<String, T> resolveAll(Class<T> targetClz) {
        return container.resolveAll(targetClz);
    }

    public static <T> T resolve(Class<T> targetClz) {
        return container.resolve(targetClz);
    }
}

package org.enodeframework;

import org.enodeframework.common.container.IObjectContainer;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Spring Ioc容器
 *
 * @author anruence@gmail.com
 */
public class ObjectContainer {
    public static IObjectContainer container;

    public static <T> Map<String, T> resolveAll(Class<T> targetClz) {
        Assert.notNull(container, "ObjectContainer can not be null");
        return container.resolveAll(targetClz);
    }

    public static <T> T resolve(Class<T> targetClz) {
        Assert.notNull(container, "ObjectContainer can not be null");
        return container.resolve(targetClz);
    }
}

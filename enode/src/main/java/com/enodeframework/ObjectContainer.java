package com.enodeframework;

import com.enodeframework.common.container.IObjectContainer;
import io.vertx.core.Vertx;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Spring Ioc容器
 *
 * @author anruence@gmail.com
 */
public class ObjectContainer {
    public static IObjectContainer container;

    public static Vertx vertx = Vertx.vertx();

    public static <T> Map<String, T> resolveAll(Class<T> targetClz) {
        Assert.notNull(container);
        return container.resolveAll(targetClz);
    }

    public static <T> T resolve(Class<T> targetClz) {
        Assert.notNull(container);
        return container.resolve(targetClz);
    }
}

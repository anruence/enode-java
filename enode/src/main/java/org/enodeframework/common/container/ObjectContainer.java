package org.enodeframework.common.container;

import org.enodeframework.common.utilities.Ensure;

import java.util.Map;

/**
 * @author anruence@gmail.com
 */
public class ObjectContainer {

    public static IObjectContainer INSTANCE;

    public static String[] BASE_PACKAGES;

    public static <T> Map<String, T> resolveAll(Class<T> targetClz) {
        Ensure.notNull(INSTANCE, "ObjectContainer can not be null");
        return INSTANCE.resolveAll(targetClz);
    }

    public static <T> T resolve(Class<T> targetClz) {
        Ensure.notNull(INSTANCE, "ObjectContainer can not be null");
        return INSTANCE.resolve(targetClz);
    }
}

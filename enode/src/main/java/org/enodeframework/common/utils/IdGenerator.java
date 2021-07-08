package org.enodeframework.common.utils;

import org.enodeframework.common.extensions.SnowFlake;

import java.util.Objects;

/**
 * @author anruence@gmail.com
 */
public class IdGenerator {

    private static final SnowFlake IDENTIFIER_GENERATOR = new SnowFlake();

    public static String nextId() {
        return Objects.toString(id(), "");
    }

    public static long id() {
        return IDENTIFIER_GENERATOR.nextId();
    }
}

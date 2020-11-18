package org.enodeframework.common.utilities;

/**
 * @author anruence@gmail.com
 */
public class IdGenerator {

    private static final SnowFlake IDENTIFIER_GENERATOR = new SnowFlake();

    public static String nextId() {
        return String.valueOf(id());
    }

    public static long id() {
        return IDENTIFIER_GENERATOR.nextId();
    }
}

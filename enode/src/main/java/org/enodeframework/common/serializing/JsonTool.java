package org.enodeframework.common.serializing;

/**
 * @author anruence@gmail.com
 */
public class JsonTool {

    public static String serialize(Object obj) {
        return JacksonSerialization.serialize(obj);
    }

    public static <T> T deserialize(String aSerialization, Class<T> aType) {
        return JacksonSerialization.deserialize(aSerialization, aType);
    }
}
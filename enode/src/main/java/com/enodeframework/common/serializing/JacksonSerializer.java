package com.enodeframework.common.serializing;

/**
 * @author anruence@gmail.com
 */
public class JacksonSerializer implements IJsonSerializer {

    @Override
    public String serialize(Object obj) {
        return JsonTool.serialize(obj);
    }

    @Override
    public <T> T deserialize(String aSerialization, Class<T> aType) {
        return JsonTool.deserialize(aSerialization, aType);
    }
}

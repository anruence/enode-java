package org.enodeframework.common.serializing;

public interface ISerializeService {

    <T> T deserialize(String json, Class<T> type);

    String serialize(Object target);
}
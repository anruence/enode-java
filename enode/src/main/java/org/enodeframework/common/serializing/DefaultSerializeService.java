package org.enodeframework.common.serializing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.enodeframework.common.exception.EnodeRuntimeException;

public class DefaultSerializeService implements ISerializeService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        //序列化时，跳过null属性
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //序列化时，遇到空bean（无属性）时不会失败
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //反序列化时，遇到未知属性（在bean上找不到对应属性）时不会失败
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //通过fields来探测（不通过标准getter探测）
        MAPPER.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
    }

    @Override
    public <T> T deserialize(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new EnodeRuntimeException(e);
        }
    }

    @Override
    public String serialize(Object target) {
        try {
            return MAPPER.writeValueAsString(target);
        } catch (JsonProcessingException e) {
            throw new EnodeRuntimeException(e);
        }
    }
}

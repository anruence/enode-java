package org.enodeframework.common.serializing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Json静态工具（基于jackson）
 * 考虑到大多数使用场景，所以：反序列化方法会吞掉异常；序列化方法会抛出异常
 */
public class JacksonSerialization {
    private static final Logger logger = LoggerFactory.getLogger(JacksonSerialization.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        //序列化时，跳过null属性
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //序列化时，遇到空bean（无属性）时不会失败
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //反序列化时，遇到未知属性（在bean上找不到对应属性）时不会失败
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //反序列化时，将空数组([])当做null来处理（以便把空数组反序列化到对象属性上——对php生成的json的map属性很有用）
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        //通过fields来探测（不通过标准getter探测）
        mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
    }

    public static <T> T deserialize(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            logger.warn("jackson deserialize failed. json: {} type: {}", json, type, e);
            return null;
        }
    }

    public static String serialize(Object target) {
        try {
            return mapper.writeValueAsString(target);
        } catch (JsonProcessingException e) {
            logger.warn("jackson serialize failed. target: {}", target.getClass(), e);
            return "";
        }
    }
}

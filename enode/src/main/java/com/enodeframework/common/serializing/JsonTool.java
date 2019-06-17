package com.enodeframework.common.serializing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Map;

public class JsonTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonTool.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    static {
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        //序列化时遇到无属性的bean时不报错
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //不序列化null属性
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //反序列化时遇到未知属性时不报错
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //反序列化时如果是空的数组则转化为空对象
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        //不通过属性来探测（仅通过getter/setter探测）
        OBJECT_MAPPER.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
    }

    public static <T> T deserialize(String json, Class<T> tClass) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, tClass);
        } catch (IOException e) {
            LOGGER.warn("parseToObject json to {} error,{}", tClass.getName(), e.getMessage());
            return null;
        }
    }

    public static <T> T deserialize(File file, TypeReference<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(file, clazz);
        } catch (IOException e) {
            LOGGER.warn("parseFileToObject failed. file={}", file, e);
            return null;
        }
    }

    public static <T> T deserialize(InputStream inputStream, TypeReference<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, clazz);
        } catch (IOException e) {
            LOGGER.warn("parseFileToObject failed. inputStream", e);
            return null;
        }
    }

    /**
     * 对象转json
     * 空对象会返回空字符串（而不是null）
     *
     * @param obj
     * @return
     */
    public static String serialize(Object obj) {
        if (obj == null) {
            return "";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            LOGGER.warn("serialize {} to json  error,{}", obj, e.getMessage());
            return "";
        }
    }

    /**
     * 将指定对象序列化并写入指定Writer
     *
     * @param writer
     * @param obj
     * @throws IOException
     */
    public static void writeToWriter(Writer writer, Object obj) throws IOException {
        OBJECT_MAPPER.writeValue(writer, obj);
    }

    public static void writeToStream(OutputStream stream, Object obj) throws IOException {
        OBJECT_MAPPER.writeValue(stream, obj);
    }

    /* =================== 忍痛将jackson的对象暴露出去 ====================== */

    /**
     * 构建一个新的jackson的ObjectNode
     *
     * @return
     */
    public static ObjectNode newObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    /**
     * 构建一个新的jackson的ArrayNode
     *
     * @return
     */
    public static ArrayNode newArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 将指定字符串（内容预期为json格式）转为jackson的JsonNode树结构 默认值为array
     *
     * @param jsonContent
     * @return
     */
    public static ArrayNode parseToArrayNode(String jsonContent) {
        if (Strings.isNullOrEmpty(jsonContent)) {
            return newArrayNode();
        }
        try {
            return (ArrayNode) OBJECT_MAPPER.readTree(jsonContent);
        } catch (IOException e) {
            LOGGER.warn("parseToArrayNode error. jsonContent={}", jsonContent, e);
            return newArrayNode();
        }
    }

    /**
     * 将指定字符串（内容预期为json格式）转为jackson的JsonNode树结构 默认值为object
     *
     * @param jsonContent
     * @return
     */
    public static ObjectNode parseToObjectNode(String jsonContent) {
        if (Strings.isNullOrEmpty(jsonContent)) {
            return newObjectNode();
        }
        try {
            return (ObjectNode) OBJECT_MAPPER.readTree(jsonContent);
        } catch (IOException e) {
            LOGGER.warn("parseToObjectNode error. jsonContent={}", jsonContent, e);
            return newObjectNode();
        }
    }

    public static JsonFactory sharedJsonFactory() {
        return JSON_FACTORY;
    }

    public static Map<String, Object> object2Map(Object o) {
        return OBJECT_MAPPER.convertValue(o, Map.class);
    }
}

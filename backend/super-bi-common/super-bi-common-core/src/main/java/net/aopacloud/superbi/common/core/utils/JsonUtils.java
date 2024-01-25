package net.aopacloud.superbi.common.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import static net.aopacloud.superbi.common.core.config.JacksonConfiguration.mapper;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/7/31 20:38
 */
public class JsonUtils {
    @SneakyThrows
    public static String toJsonString(Object o) {
        return toJsonString(o, false);
    }

    /**
     * 对象转换为json字符串
     *
     * @param o      要转换的对象
     * @param format 是否格式化json
     */
    @SneakyThrows
    public static String toJsonString(Object o, boolean format) {
        if (o instanceof Number) {
            return o.toString();
        }
        if (o instanceof String) {
            return (String) o;
        }
        if (format) {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        }
        return mapper.writeValueAsString(o);
    }

    @SneakyThrows
    public static <T> T fromJson(String str, Class<T> clazz) {
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            return null;
        }
        return mapper.readValue(str, clazz);
    }

    /**
     * 字符串转换为指定对象，并增加泛型转义
     * 例如：List<Integer> test = toObject(jsonStr, List.class, Integer.class);
     *
     * @param json             json字符串
     * @param parametrized     目标对象
     * @param parameterClasses 泛型对象
     */
    @SneakyThrows
    public static <T> T toObject(String json, Class<?> parametrized, Class<?>... parameterClasses) {
        if (org.apache.commons.lang3.StringUtils.isBlank(json) || parametrized == null) {
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
        return mapper.readValue(json, javaType);
    }

    @SneakyThrows
    public static <T> T fromJson(String str, TypeReference<T> typeReference) {
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            return null;
        }
        return mapper.readValue(str, typeReference);
    }

    /**
     * 字符串转换为JsonNode对象
     *
     * @param json json字符串
     */
    @SneakyThrows
    public static JsonNode parse(String json) {
        if (org.apache.commons.lang3.StringUtils.isBlank(json)) {
            return null;
        }
        return mapper.readTree(json);
    }

    @SneakyThrows
    public static Integer getInteger(Object object) {
        if (object == null) {
            return null;
        }
        return Integer.valueOf(mapper.writeValueAsString(object));
    }

    /**
     * 对象转换为map对象
     *
     * @param o 要转换的对象
     */
    public static <K, V> Map<K, V> toMap(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return toObject((String) o, Map.class);
        }
        return mapper.convertValue(o, Map.class);
    }

    /**
     * json字符串转换为list对象
     *
     * @param json json字符串
     */
    @SneakyThrows
    public static <T> List<T> toList(String json) {
        if (org.apache.commons.lang3.StringUtils.isBlank(json)) {
            return null;
        }
        return mapper.readValue(json, List.class);
    }

    /**
     * json字符串转换为list对象，并指定元素类型
     *
     * @param json json字符串
     * @param cls  list的元素类型
     */
    @SneakyThrows
    public static <T> List<T> toList(String json, Class<T> cls) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, cls);
        return mapper.readValue(json, javaType);
    }
}

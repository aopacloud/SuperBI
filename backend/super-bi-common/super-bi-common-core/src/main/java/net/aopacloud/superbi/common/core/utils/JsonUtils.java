package net.aopacloud.superbi.common.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static net.aopacloud.superbi.common.core.config.JacksonConfiguration.mapper;

/**
 * Json utils
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
     * convert Object to Json String
     *
     * @param o  Object to convert
     * @param format whether to format
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

    /**
     * convert Json String to bean Object
     * @param str
     * @param clazz
     * @return
     */
    @SneakyThrows
    public static <T> T fromJson(String str, Class<T> clazz) {
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            return null;
        }
        return mapper.readValue(str, clazz);
    }

    /**
     * convert Json String to bean Object，and add generic escape
     * for example: List<Integer> test = toObject(jsonStr, List.class, Integer.class);
     *
     * @param json             json string
     * @param parametrized     bean class
     * @param parameterClasses generic class
     */
    @SneakyThrows
    public static <T> T toObject(String json, Class<?> parametrized, Class<?>... parameterClasses) {
        if (org.apache.commons.lang3.StringUtils.isBlank(json) || parametrized == null) {
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
        return mapper.readValue(json, javaType);
    }

    /**
     * convert Json String to List
     *
     * @param json
     */
    @SneakyThrows
    public static <T> List<T> toList(String json) {
        if (org.apache.commons.lang3.StringUtils.isBlank(json)) {
            return null;
        }
        return mapper.readValue(json, List.class);
    }

    /**
     * convert Json String to List, and specify the type of list elements
     *
     * @param json
     * @param cls
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

package com.wei.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }

        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();

                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(obj, convertType(field, fieldValue));
                } catch (NoSuchFieldException ignored) {
                    // 如果类中没有对应字段，则忽略
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Map 转对象失败", e);
        }
    }

    private static Object convertType(Field field, Object value) {
        if (value == null) {
            return null;
        }
        Class<?> type = field.getType();

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value.toString());
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value.toString());
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value.toString());
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }
        return value;
    }

    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();

        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    map.put(field.getName(), field.get(obj));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field: " + field.getName(), e);
                }
            }
            clazz = clazz.getSuperclass(); // 处理继承的字段
        }

        return map;
    }


    private static final ObjectMapper objectMapper = new ObjectMapper(); // JSON 序列化工具

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败 , json = " + json, e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败 ", e);
        }
    }
}

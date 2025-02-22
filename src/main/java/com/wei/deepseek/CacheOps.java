package com.wei.deepseek;

import com.wei.MyUser;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

// 编程式API
public class CacheOps {
    public static <T> T withCache(String cacheName, Object key, Supplier<T> loader) {
        return CacheManager.getCache(cacheName).get(key, loader);
    }

    public static <T> T withCache(String cacheName, Function<KeyBuilder, Object> keyBuilder, Supplier<T> loader) {
        Object key = keyBuilder.apply(new KeyBuilder());
        return withCache(cacheName, key, loader);
    }

    public static class KeyBuilder {
        public Object build(Object... parts) {
            return Arrays.hashCode(parts);
        }
    }
}
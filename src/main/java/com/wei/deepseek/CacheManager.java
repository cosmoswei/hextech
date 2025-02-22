package com.wei.deepseek;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

// 缓存管理器
public class CacheManager {
    private static final Map<String, Cache> caches = new ConcurrentHashMap<>();

    public static Cache getCache(String name) {
        return caches.computeIfAbsent(name, s -> null);
    }
}
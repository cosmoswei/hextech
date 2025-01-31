package com.wei.cache.cache2;

public interface CacheStrategy {
    <T> T get(String key, Class<T> type, CacheLoader<T> loader);
    void put(String key, Object value);
    void delete(String key);
}
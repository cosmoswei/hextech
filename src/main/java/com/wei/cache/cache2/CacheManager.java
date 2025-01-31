package com.wei.cache.cache2;

public class CacheManager {
    private final CacheStrategy cacheStrategy;

    public CacheManager(CacheStrategy strategy) {
        this.cacheStrategy = strategy;
    }

    public <T> T get(String key, Class<T> type, CacheLoader<T> loader) {
        return cacheStrategy.get(key, type, loader);
    }

    public void put(String key, Object value) {
        cacheStrategy.put(key, value);
    }

    public void delete(String key) {
        cacheStrategy.delete(key);
    }
}
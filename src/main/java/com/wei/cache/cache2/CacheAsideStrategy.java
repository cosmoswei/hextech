package com.wei.cache.cache2;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wei.json.JsonUtil;
import org.redisson.api.RMapCache;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CacheAsideStrategy implements CacheStrategy {
    private final LoadingCache<String, Object> localCache;
    private final RMapCache<String, String> redisCache;
    private final Duration redisTtl = Duration.ofMinutes(30); // Redis 缓存时间

    public CacheAsideStrategy(RMapCache<String, String> redisCache) {
        this.redisCache = redisCache;
        this.localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .build(key -> null); // 本地缓存默认不主动加载
    }

    @Override
    public <T> T get(String key, Class<T> type, CacheLoader<T> loader) {
        // 1. 先查本地缓存
        T value = (T) localCache.getIfPresent(key);
        if (value != null) return value;

        // 2. 本地缓存未命中，查 Redis（Redisson）
        String redisValue = redisCache.get(key);
        if (redisValue != null) {
            value = JsonUtil.fromJson(redisValue, type);
            localCache.put(key, value);
            return value;
        }

        // 3. Redis 也未命中，查数据库
        value = loader.load();
        if (value != null) {
            redisCache.put(key, JsonUtil.toJson(value), redisTtl.getSeconds(), TimeUnit.SECONDS);
            localCache.put(key, value);
        }
        return value;
    }

    @Override
    public void put(String key, Object value) {
        redisCache.put(key, JsonUtil.toJson(value), redisTtl.getSeconds(), TimeUnit.SECONDS);
        localCache.put(key, value);
    }

    @Override
    public void delete(String key) {
        redisCache.remove(key);
        localCache.invalidate(key);
    }


}
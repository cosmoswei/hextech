package com.wei.cache.cache1;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wei.json.JsonUtil;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class CacheManager {
    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);
    private final Cache<String, Object> localCache;
    private final RMapCache<String, String> redisCache;
    private final Duration redisTtl = Duration.ofMinutes(30); // Redis 缓存时间

    public CacheManager(RedissonClient redissonClient) {
        this.redisCache = redissonClient.getMapCache("my-cache");
        this.localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .build();
    }

    public <T> T get(String key, Class<T> type, Supplier<T> dbLoader) {
        // 1️⃣ 先查本地缓存
        T value = (T) localCache.getIfPresent(key);
        if (value != null) return value;

        log.info("本地缓存未命中！");
        // 2️⃣ 本地缓存未命中，查 Redis
        String redisValue = redisCache.get(key);
        if (redisValue != null) {
            log.info("Redis命中！");
            value = JsonUtil.fromJson(redisValue, type);
            localCache.put(key, value);
            return value;
        }

        // 3️⃣ Redis 也未命中，查数据库
        log.info("Redis 也未命中！");
        value = dbLoader.get();
        if (value != null) {
            redisCache.put(key, JsonUtil.toJson(value), redisTtl.getSeconds(), TimeUnit.SECONDS);
            localCache.put(key, value);
        }
        return value;
    }

    public void put(String key, Object value) {
        redisCache.put(key, JsonUtil.toJson(value), redisTtl.getSeconds(), TimeUnit.SECONDS);
        localCache.put(key, value);
    }

    public void delete(String key) {
        redisCache.remove(key);
        localCache.invalidate(key);
    }
}
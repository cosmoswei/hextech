package com.wei.cappuccino;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCache implements CacheBase {

    private static final Logger log = LoggerFactory.getLogger(RedisCache.class);

    private final RMapCache<String, Object> redisCache;
    private final RedissonClient client;

    public RedisCache(RedissonClient client, String topic) {
        this.client = client;
        this.redisCache = client.getMapCache(topic);
    }

    @Override
    public void put(String key, Object object) {
        redisCache.put(key, object);
    }

    @Override
    public Object get(String key) {
        return redisCache.get(key);
    }

    @Override
    public void delete(String key) {
        log.info("delete caffeine cache key = {}", key);
        redisCache.remove(key);
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }
}

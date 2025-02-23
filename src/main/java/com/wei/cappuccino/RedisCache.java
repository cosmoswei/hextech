package com.wei.cappuccino;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

public class RedisCache implements CacheBase {

    private final RMapCache<String, Object> redisCache;


    public RedisCache(RedissonClient client) {
        this.redisCache = client.getMapCache("my-cache");
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
        redisCache.remove(key);
    }
}

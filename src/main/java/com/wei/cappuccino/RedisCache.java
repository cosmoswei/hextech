package com.wei.cappuccino;

import org.redisson.api.RMapCache;

public class RedisCache implements CacheBase {

    private final static RMapCache<String, Object> redisCache = RedissonFactory.create().getMapCache("my-cache");

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

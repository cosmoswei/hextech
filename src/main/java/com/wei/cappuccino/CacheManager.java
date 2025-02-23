package com.wei.cappuccino;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class CacheManager {

    private final CacheBase l1Cache;
    private final CacheBase l2Cache;
    private final MessageNotify messageNotify;

    public CacheManager(CacheBase l1Cache, CacheBase l2Cache) {
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
        this.messageNotify = new RedisStreamNotify();
    }

    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);

    public <T> T get(String key, Class<T> type, Supplier<T> supplier) {
        // 先查本地缓存
        T value = (T) l1Cache.get(key);
        if (value != null) return value;

        log.info("本地缓存 未命中！");
        // 本地缓存未命中，查 Redis
        Object l2value = l2Cache.get(key);
        if (l2value != null) {
            log.info("Redis 命中！");
            l1Cache.put(key, l2value);
            return value;
        }

        // Redis 也未命中，查数据库
        log.info("Redis 也未命中！");
        value = supplier.get();
        if (value != null) {
            put(key, value);
        }
        return value;
    }

    public void put(String key, Object value) {
        l1Cache.put(key, value);
        l2Cache.put(key, value);
    }

    public void delete(String key) {
        l1Cache.delete(key);
        l2Cache.delete(key);
    }

    /**
     * 处理监听失效的消息
     */
    private boolean handlerListener(NotifyMsg msg) {
        try {
            delete(msg.getKey());
            return true;
        } catch (Exception e) {
            log.error("delete {} err msg = {}", msg.getKey(), e.getMessage());
        }
        return false;
    }

}
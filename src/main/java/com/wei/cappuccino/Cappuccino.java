package com.wei.cappuccino;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Cappuccino {

    private final CacheBase l1Cache;
    private final CacheBase l2Cache;

    public Cappuccino(CacheBase l1Cache, CacheBase l2Cache) {
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
    }

    private static final Logger log = LoggerFactory.getLogger(Cappuccino.class);

    public void fail(String key, Consumer<Object> consumer) {
        consumer.accept(key);
        delete(key);
    }

    public <T> T get(String key, Supplier<T> supplier) {
        // 先查本地缓存
        T value = (T) l1Cache.get(key);
        if (value != null) {
            log.info("l1 Cache hit！");
            return value;
        }

        log.info("l1 Cache  not hit！");
        // 本地缓存未命中，查 Redis
        T l2value = (T) l2Cache.get(key);
        if (l2value != null) {
            log.info("l2 Cache hit！");
            l1Cache.put(key, l2value);
            return l2value;
        }

        // Redis 也未命中，查数据库
        log.info("l2 Cache  not hit！");
        value = supplier.get();
        if (value != null) {
            put(key, value);
            return value;
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
        log.info("handlerListener msg = {}", msg);
        try {
            delete(msg.getKey());
            return true;
        } catch (Exception e) {
            log.error("delete {} err msg = {}", msg.getKey(), e.getMessage());
        }
        return false;
    }

    public void shutdown() {
        l1Cache.shutdown();
        l2Cache.shutdown();
    }


}
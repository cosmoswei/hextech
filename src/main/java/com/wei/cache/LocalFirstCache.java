package com.wei.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class LocalFirstCache implements FirstCache {

    private static final Cache<String, Object> LOCAL_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Override
    public void put(String key, Object object) {
        LOCAL_CACHE.put(key, object);
    }

    @Override
    public Object get(String key) {
        return LOCAL_CACHE.getIfPresent(key);
    }


    @Override
    public void delete(String key) {
        LOCAL_CACHE.invalidate(key);
    }

    public void sendInvalidMsg(String key) {
        // 广播发送一个失效的消息
    }


    /**
     * 处理监听失效的消息
     */
    private void handlerListenerInvalidMsg(Object object) {

    }
}

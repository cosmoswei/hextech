package com.wei.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class LocalFirstCache implements FirstCache {

    private static final Cache<String, Object> LOCAL_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    private static final Logger log = LoggerFactory.getLogger(LocalFirstCache.class);

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
        NoticeMsg noticeMsg = new NoticeMsg();
        noticeMsg.setKey(key);
    }


    /**
     * 处理监听失效的消息
     */
    private boolean handlerListenerInvalidMsg(NoticeMsg msg) {
        try {
            delete(msg.getKey());
            return true;
        } catch (Exception e) {
            log.info("delete {} err msg = {}", msg.getKey(), e.getMessage());
        }
        return false;
    }
}

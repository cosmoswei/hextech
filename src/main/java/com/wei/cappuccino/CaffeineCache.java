package com.wei.cappuccino;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaffeineCache implements CacheBase {

    MessageNotify messageNotify;

    private static Cache<String, Object> LOCAL_CACHE;

    public CaffeineCache(Cache<String, Object> cache, MessageNotify messageNotify) {
        this.messageNotify = messageNotify;
        this.LOCAL_CACHE = cache;
    }


    private static final Logger log = LoggerFactory.getLogger(CaffeineCache.class);

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
        // 广播发送一个失效的消息
        NotifyMsg notifyMsg = new NotifyMsg();
        notifyMsg.setKey(key);
        messageNotify.broadcast(notifyMsg);
    }

    /**
     * 处理监听失效的消息
     */
    private boolean listen(NotifyMsg msg) {
        try {
            delete(msg.getKey());
            return true;
        } catch (Exception e) {
            log.info("delete {} err msg = {}", msg.getKey(), e.getMessage());
        }
        return false;
    }
}

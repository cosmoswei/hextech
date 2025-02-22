package com.wei.cappuccino;

public class CompletableCache {

    private CacheBase firstCache;
    private CacheBase secondCache;

    public CompletableCache(CacheBase firstCache, CacheBase secondCache) {

        this.firstCache = firstCache;
        this.secondCache = secondCache;

    }

    void put(String key, Object object) {
        // 放入数据库（MySQL）

        // 放入一级缓存
        if (null != firstCache) {
            firstCache.put(key, object);
        }

        // 放入二级缓存
        if (null != secondCache) {
            secondCache.put(key, object);
        }
    }

    Object get(String key) {
        Object firstValue = this.firstCache.get(key);
        if (null != firstValue) {
            return firstValue;
        }

        Object secondValue = secondCache.get(key);
        if (null != secondValue) {
            firstCache.put(key, secondValue);
            return secondValue;
        }
        // 缓存里面没有 则去数据库里找
        Object value = new Object();
        put(key, value);
        return value;
    }

    void remove(String key) {
        // 更新数据库

        // 删除分布式缓存； 发送MQ
        secondCache.delete(key);

        // 删除本地缓存：广播到所有节点
        firstCache.delete(key);
    }
}

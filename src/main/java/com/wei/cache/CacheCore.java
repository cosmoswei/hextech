package com.wei.cache;

import java.util.ArrayList;
import java.util.List;

public class CacheCore {

    List<Cache> cacheLevels = new ArrayList<>();

    void put(String key, Object object) {
        // 放入数据库（MySQL）

        // 放入多层缓存
        for (Cache cacheLevel : cacheLevels) {
            cacheLevel.put(key, object);
        }
    }

    Object get(String key) {
        for (Cache cacheLevel : cacheLevels) {
            Object value = cacheLevel.get(key);
            if (null != value) {
                return value;
            } else {
                // 放到上一层 Cache
            }
        }
        // 缓存里面没有 则去数据库里找
        Object value = new Object();
        put(key, value);
        return value;
    }

    void remove(String key) {
        // 更新数据库

        // 删除分布式缓存； 发送MQ

        // 删除本地缓存
    }
}

package com.wei.cappuccino;

public interface CacheBase {
    void put(String key, Object object);

    Object get(String key);

    void delete(String key);

    void shutdown();

}

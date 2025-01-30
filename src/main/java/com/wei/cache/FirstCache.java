package com.wei.cache;

public interface FirstCache {
    void put(String key, Object object);

    Object get(String key);

    void delete(String key);
}

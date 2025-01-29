package com.wei.cache;

public interface Cache {
    void put(String key, Object object);

    Object get(String key);

    void remove(String key);
}

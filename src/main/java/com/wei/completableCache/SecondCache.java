package com.wei.completableCache;

public interface SecondCache {
    void put(String key, Object object);

    Object get(String key);

    void delete(String key);
}

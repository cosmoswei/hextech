package com.wei.cappuccino;

import java.util.function.Supplier;

// 缓存接口
public interface Cache {
    <T> T get(Object key, Supplier<T> loader);

    void put(Object key, Object value);
}
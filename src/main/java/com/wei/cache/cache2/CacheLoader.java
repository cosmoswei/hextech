package com.wei.cache.cache2;

@FunctionalInterface
public interface CacheLoader<T> {
    T load();
}
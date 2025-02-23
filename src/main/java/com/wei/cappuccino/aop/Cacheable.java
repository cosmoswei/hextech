package com.wei.cappuccino.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 缓存注解定义
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {
    String cacheName();

    String keyPattern() default "";

    int ttl() default 300; // 秒
}
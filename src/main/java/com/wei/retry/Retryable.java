package com.wei.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 声明式注解
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retryable {
    int maxAttempts() default 3;

    long delay() default 1000; // 毫秒

    Class<? extends Throwable>[] retryFor() default {Exception.class};

    Class<? extends Throwable>[] abortFor() default {};
}
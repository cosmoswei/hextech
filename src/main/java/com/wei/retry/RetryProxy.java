package com.wei.retry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;

// AOP处理器
public class RetryProxy implements InvocationHandler {
    private final Object target;

    public static <T> T createProxy(T target) {
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new RetryProxy(target));
    }

    private RetryProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Retryable annotation = method.getAnnotation(Retryable.class);
        if (annotation == null) {
            return method.invoke(target, args);
        }

        RetryPolicy policy = new RetryPolicy.Builder()
                .maxAttempts(annotation.maxAttempts())
                .delay(Duration.ofMillis(annotation.delay()))
                .retryOn(annotation.retryFor())
                .abortOn(annotation.abortFor())
                .build();

        return policy.execute(() -> method.invoke(target, args));
    }
}
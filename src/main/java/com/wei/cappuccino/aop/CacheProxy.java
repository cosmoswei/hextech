package com.wei.cappuccino.aop;

import java.lang.reflect.Method;
import java.util.Arrays;

// AOP处理器
public class CacheProxy implements java.lang.reflect.InvocationHandler {

    private final Object target;

    public static <T> T createProxy(T target) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new CacheProxy(target));
    }

    private CacheProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Cacheable annotation = method.getAnnotation(Cacheable.class);
        if (annotation == null) {
            return method.invoke(target, args);
        }

        String key = generateKey(annotation.keyPattern(), method, args);

        return null;
    }

    private String generateKey(String pattern, Method method, Object[] args) {
        if (pattern.isEmpty()) {
            return method.getName() + Arrays.hashCode(args);
        }
        // 简单实现参数替换，例如 #0 表示第一个参数
        String result = pattern;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("#" + i, args[i].toString());
        }
        return result;
    }
}
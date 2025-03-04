package com.wei.seqMq;

import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class StreamConsumerRegistrar {

    public static void register(StreamMessageListenerContainer container, String... basePackages) {
        Reflections reflections = new Reflections(basePackages);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(StreamConsumer.class);
        for (Class<?> clazz : classes) {
            if (StreamListener.class.isAssignableFrom(clazz)) {
                try {
                    StreamConsumer annot = clazz.getAnnotation(StreamConsumer.class);
                    StreamListener<?> listener = (StreamListener<?>) clazz.getDeclaredConstructor().newInstance();
                    // 设置 autoAck 属性
                    setAutoAckProperty(listener, annot.autoAck());
                    // 构建 ConsumerInfo
                    ConsumerInfo consumerInfo = new ConsumerInfo(annot.streamName(), annot.groupName());



                    // 注册到容器
                    if (annot.autoAck()) {
                        container.receiveAutoAck(consumerInfo, listener);
                    } else {
                        container.receive(consumerInfo, listener, false);
                    }
                } catch (InstantiationException | IllegalAccessException |
                         NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void setAutoAckProperty(StreamListener<?> listener, boolean autoAck) {
        try {
            // 尝试调用 setActAck 方法
            Method setMethod = listener.getClass().getMethod("setActAck", boolean.class);
            setMethod.invoke(listener, autoAck);
        } catch (NoSuchMethodException e) {
            // 方法不存在，尝试直接设置字段
            try {
                Field field = listener.getClass().getDeclaredField("actAck");
                field.setAccessible(true);
                field.setBoolean(listener, autoAck);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new RuntimeException("Failed to set autoAck for listener: " + listener.getClass(), ex);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error setting autoAck property", e);
        }
    }
}
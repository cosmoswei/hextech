package com.wei.seqMq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StreamConsumer {
    String streamName();      // 流键

    String groupName();  // 消费者组

    boolean autoAck() default true; // 是否自动确认
}
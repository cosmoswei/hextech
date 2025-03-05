package com.wei.seqMq.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StreamConsumer {

    /**
     * 流键
     */
    String streamName();

    /**
     * 消费者组
     */
    String groupName();

    /**
     * 是否自动确认
     */
    boolean autoAck() default true;

    /**
     * 广播
     */
    boolean broadcast() default false;
}
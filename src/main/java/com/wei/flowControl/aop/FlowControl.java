package com.wei.flowControl.aop;


import com.wei.flowControl.constant.FlowControlConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlowControl {

    /**
     * 限流次数
     */
    int count() default 0;

    /**
     * 间隔时间，单位毫秒
     */
    int interval() default 1000;


    /**
     * 限流Key 默认为方法名
     */
    String key() default "";

    /**
     * 限流器类型
     */
    String type() default FlowControlConstant.COUNTER;

    /**
     * 限流异常提示
     */
    String limitMsg() default "系统服务繁忙";

    /**
     * 回调方法
     */
    String callback() default "";
}

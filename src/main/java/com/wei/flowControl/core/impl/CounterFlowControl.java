package com.wei.flowControl.core.impl;

import com.wei.flowControl.core.AbstractFlowControl;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 计数器限流
 */
@Slf4j
public class CounterFlowControl extends AbstractFlowControl {

    /**
     * 最后更新时间
     */
    private static long lastTime = System.currentTimeMillis();

    /**
     * 计数器
     */
    private final AtomicLong accumulator = new AtomicLong(0);

    public static CounterFlowControl newCounterFlowControl() {
        return new CounterFlowControl();
    }


    @Override
    public boolean canPass() {

        long nowTime = System.currentTimeMillis();
        // 在当前的事件窗口
        if (nowTime < lastTime + interval) {
            long accumulatorCount = accumulator.incrementAndGet();
            return accumulatorCount <= getFlowControlCount();
        } else {
            // 不在当前的事件窗口
            synchronized (this) {
                if (nowTime > lastTime + interval) {
                    accumulator.set(0);
                    lastTime = nowTime;
                }
            }
            return true;
        }
    }
}

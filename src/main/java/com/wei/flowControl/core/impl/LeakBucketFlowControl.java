package com.wei.flowControl.core.impl;


import com.wei.flowControl.core.AbstractFlowControl;
import com.wei.flowControl.core.FlowControl;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class LeakBucketFlowControl extends AbstractFlowControl {


    private long lastTime = System.currentTimeMillis();

    private static int leakRate;

    private static int capacity;

    private final AtomicInteger water = new AtomicInteger(0);

    @Override
    public FlowControl init(int count, int interval) {
        FlowControl flowControl = super.init(count, interval);
        int qps = count / interval;
        leakRate = qps;
        capacity = qps;
        return flowControl;
    }

    /**
     * 漏桶的逻辑是与上一次执行漏桶的差值 * QPS 作为，已经流出去多少水，在根据，再跟水桶水位取一个较小值，赋给水位线
     */

    private final ReentrantLock lock = new ReentrantLock(); // 保证线程安全


    @Override
    public boolean canPass() {

        // 自适应
        capacity = getQps();

        lock.lock();

        try {
            if (water.get() == 0) {
                lastTime = System.currentTimeMillis();
                water.addAndGet(1);
                return true;
            }
            // 将时间差值 * QPS 为需要漏掉的水，降低漏桶水位线
            int waterLeaked = ((int) ((System.currentTimeMillis() - lastTime) / 1000)) * leakRate;
            int waterLeft = water.get() - waterLeaked;
            water.set(Math.max(0, waterLeft));
            // 水未满，放行
            if (water.get() < capacity) {
                lastTime = System.currentTimeMillis();
                water.addAndGet(1);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public static LeakBucketFlowControl newLeakBucketFlowControl() {
        return new LeakBucketFlowControl();
    }

}

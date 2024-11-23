package com.wei.flowControl.core.impl;


import com.wei.flowControl.core.AbstractFlowControl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowFlowControl extends AbstractFlowControl {

    /**
     * 请求时间戳列表
     */
    private final Queue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();

    /**
     * 锁
     */
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public boolean canPass() {
        lock.lock();
        try {
            cleanExpiredRequests();
            // 通过则添加时间戳到列表
            if (requestTimestamps.size() < getFlowControlCount()) {
                cleanExpiredRequests();
                requestTimestamps.offer(System.currentTimeMillis());
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void cleanExpiredRequests() {
        long currentTime = System.currentTimeMillis();
        while (!requestTimestamps.isEmpty()
                && currentTime - requestTimestamps.peek() > interval * 1000L) {
            requestTimestamps.poll();
        }
    }

    public static SlidingWindowFlowControl newSlidingWindowFlowControl() {
        return new SlidingWindowFlowControl();
    }
}

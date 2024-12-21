package com.wei.metrics.obj;

import lombok.Data;

@Data
public class ThreadMetrics {

    /**
     * 当前活动线程数量
     */
    private double liveThreads;

    /**
     * 线程峰值数量
     */
    private double peakThreads;

    /**
     * 被阻塞的线程数量
     */
    private double blockedThreads;

    /**
     * 正在运行的线程数量
     */
    private double runnableThreads;

    /**
     * 新创建的线程数量
     */
    private double newThreads;

    /**
     * 正在等待的线程数量（包括休眠、I/O等待等）
     */
    private double timedWaitingThreads;

    /**
     * 已终止的线程数量
     */
    private double terminatedThreads;

    /**
     * 处于等待状态的线程数量（例如：wait、join、lock等）
     */
    private double waitingThreads;

}
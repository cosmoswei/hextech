package com.wei.metrics.obj;

import lombok.Data;

@Data
public class ThreadMetrics {
    private double liveThreads;

    private double peakThreads;

    private double blockedThreads;

    private double runnableThreads;

    private double newThreads;

    private double timedWaitingThreads;

    private double terminatedThreads;

    private double waitingThreads;
}
package com.wei.metrics.obj;

import lombok.Data;

@Data
public class SystemMetrics {

    // CPU metrics
    private int cpuCount;
    private double cpuLoad1m;
    private double cpuLoad5m;
    private double cpuLoad15m;
    private CpuUsage cpuUsage;
    private double contextSwitches;
    private double interrupts;
    // Memory metrics
    private double memoryAvailable;
    private double memoryTotal;
    private double swapUsed;
    private double swapTotal;

    // process
    private int processCount;
    private int threadCount;

    // Disk metrics
    private long openFdCount;
    private long iops;
}


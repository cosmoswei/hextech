package com.wei.metrics.obj;

import lombok.Data;

@Data
public class SystemMetrics {

    /**
     * CPU 核心数
     */
    private int cpuCount;

    /**
     * 最近1分钟的CPU负载
     */
    private double cpuLoad1m;

    /**
     * 最近5分钟的CPU负载
     */
    private double cpuLoad5m;

    /**
     * 最近15分钟的CPU负载
     */
    private double cpuLoad15m;

    /**
     * 当前CPU使用情况（用户、系统等）
     */
    private CpuUsage cpuUsage;

    /**
     * 上下文切换次数
     */
    private double contextSwitches;

    /**
     * 中断次数
     */
    private double interrupts;

    /**
     * 可用内存（单位：字节）
     */
    private double memoryAvailable;

    /**
     * 总内存（单位：字节）
     */
    private double memoryTotal;

    /**
     * 已使用的交换空间（单位：字节）
     */
    private double swapUsed;

    /**
     * 总交换空间（单位：字节）
     */
    private double swapTotal;

    /**
     * 当前进程数量
     */
    private int processCount;

    /**
     * 当前线程数量
     */
    private int threadCount;

    /**
     * 打开的文件描述符数量
     */
    private long openFdCount;

    /**
     * 每秒输入输出操作次数（IOPS）
     */
    private long iops;

}


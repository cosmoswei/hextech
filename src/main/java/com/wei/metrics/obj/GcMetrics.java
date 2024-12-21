package com.wei.metrics.obj;

import lombok.Data;

@Data
public class GcMetrics {
    /**
     * JVM GC 次数
     */
    private int gcCount1m;
    /**
     * JVM GC 吞吐（%）
     */
    private double gcThroughput1m;
    /**
     * JVM 暂停 时间（ms）
     */
    private double gcPauseTime1m;
    /**
     * JVM 内存使用情况
     */
    private double gcMemoryPoolUsage;
    /**
     * JVM堆内存分配速率（MBS）
     */
    private double gcHeapAllocationRate;
    /**
     * JVM堆内存晋升速率（MBS）
     */
    private double gcHeapPromotedRate;
}
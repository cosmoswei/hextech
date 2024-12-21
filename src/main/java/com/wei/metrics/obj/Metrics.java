package com.wei.metrics.obj;

import lombok.Data;

@Data
public class Metrics {

    /**
     * 系统指标
     */
    private SystemMetrics systemMetrics;

    /**
     * 网络指标
     */
    private NetworkMetrics networkMetrics;

    /**
     * GC 指标
     */
    private GcMetrics gcMetrics;

    /**
     * 线程指标
     */
    private ThreadMetrics threadMetrics;
}

package com.wei.metrics.obj;

import lombok.Data;

@Data
public class Metrics {

    private SystemMetrics systemMetrics;

    private NetworkMetrics networkMetrics;

    private GcMetrics gcMetrics;

    private ThreadMetrics threadMetrics;
}

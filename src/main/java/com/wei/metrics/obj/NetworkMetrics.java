package com.wei.metrics.obj;

import lombok.Data;

@Data
public class NetworkMetrics {
    // Network metrics
    private double latency;
    private double bandwidth;
    private long mtu;
    private long speed;
    private String macaddr;
    private String[] iPv4addr;
    private String[] iPv6addr;
    private boolean hasData;
    private long packetsRecv;
    private long bytesRecv;
    private long inErrors;
    private long outErrors;
    private long bytesSent;
    private long packetsSent;
}
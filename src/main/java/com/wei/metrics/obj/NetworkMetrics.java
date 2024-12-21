package com.wei.metrics.obj;

import lombok.Data;

@Data
public class NetworkMetrics {
    // 网络性能指标
    /**
     * 网络延迟（单位：毫秒）
     */
    private double latency;

    /**
     * 网络带宽（单位：字节/秒）
     */
    private double bandwidth;

    /**
     * 最大传输单元（MTU，单位：字节）
     */
    private long mtu;

    /**
     * 网络速度（单位：比特/秒）
     */
    private long speed;

    /**
     * 网络接口的MAC地址
     */
    private String macaddr;

    /**
     * IPv4地址数组
     */
    private String[] iPv4addr;

    /**
     * IPv6地址数组
     */
    private String[] iPv6addr;

    /**
     * 是否有网络数据
     */
    private boolean hasData;

    /**
     * 接收的数据包数量
     */
    private long packetsRecv;

    /**
     * 接收的字节数
     */
    private long bytesRecv;

    /**
     * 输入错误的数量
     */
    private long inErrors;

    /**
     * 输出错误的数量
     */
    private long outErrors;

    /**
     * 发送的字节数
     */
    private long bytesSent;

    /**
     * 发送的数据包数量
     */
    private long packetsSent;

}
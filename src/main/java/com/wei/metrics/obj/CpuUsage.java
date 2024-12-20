package com.wei.metrics.obj;

import lombok.Data;

@Data
public class CpuUsage {
    /**
     * 用户使用率
     */
    private double cpuUsrUsage;
    /**
     * 系统使用率
     */
    private double cpuSysUsage;
    /**
     * 空闲率
     */
    private double cpuIdle;
}
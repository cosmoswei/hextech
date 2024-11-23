package com.wei.flowControl.core;

import com.wei.flowControl.exception.FlowControlException;

public abstract class AbstractFlowControl implements FlowControl {

    // 自适应开关
    private boolean adaptable = false;

    // 服务质量
    private float qos = 0f;

    /**
     * 流控数量
     */
    private int flowControlCount;

    public int getFlowControlCount() {
        if (Boolean.TRUE.equals(adaptable)) {
            return (int) (this.flowControlCount * getQoS());
        }
        return this.flowControlCount;
    }

    public int getQps() {
        return getFlowControlCount() / interval;
    }

    private float getQoS() {
        return qos;
    }

    /**
     * 流控时间间隔，单位：秒
     */
    public int interval;

    @Override
    public FlowControl init(int count, int interval) {
        if (interval <= 0) {
            throw new FlowControlException("interval must be greater than 0");
        }
        if (count <= 0) {
            throw new FlowControlException("count must be greater than 0");
        }
        this.flowControlCount = count;
        this.interval = interval;
        return this;
    }
}

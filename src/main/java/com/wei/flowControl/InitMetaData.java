package com.wei.flowControl;


import lombok.Data;

@Data
public class InitMetaData {

    /**
     * 被流控的对象，唯一
     */
    public String resourceKey;

    /**
     * 流控类型
     */
    public String FlowControlType;

    /**
     * 流控数量
     */
    public int count;

    /**
     * 流控时间间隔，单位：秒
     */
    public int interval;

    public InitMetaData(String resourceKey, String flowControlType, int count, int interval) {
        this.resourceKey = resourceKey;
        FlowControlType = flowControlType;
        this.count = count;
        this.interval = interval;
    }

    public InitMetaData() {
    }
}

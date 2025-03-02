package com.wei.seqMq;

import lombok.Data;

@Data
public class ConsumerInfo {
    /**
     * 流（Topic）名
     */
    private String streamName;
    /**
     * 消费者组名
     */
    private String groupName;
    /**
     * 消费者名
     */
    private String consumerName;

    public ConsumerInfo(String streamName, String groupName) {
        this.streamName = streamName;
        this.groupName = groupName;
    }

    public ConsumerInfo() {
    }
}

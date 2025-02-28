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

}

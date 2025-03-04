package com.wei.seqMq;

import lombok.Data;

@Data
public class SeqMessageQueueConfig {
    private String redisUri;
    private String redisPassword;
    private String scanPackage;
    private ConsumerInfo consumerInfo;
}

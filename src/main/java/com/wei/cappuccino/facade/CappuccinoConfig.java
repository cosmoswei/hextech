package com.wei.cappuccino.facade;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CappuccinoConfig {
    private String topic;
    private Long caffeineTtl;
    private Integer caffeineMacSize;
    private String redisUri;
    private String redisPassword;
    private String mqNameAddr;
}

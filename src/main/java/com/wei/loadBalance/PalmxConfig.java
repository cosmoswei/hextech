package com.wei.loadBalance;

public class PalmxConfig {
    public static LoadBalancerType getLoadBalanceType() {
        String property = "round-robin";
        if ("round-robin".equals(property)) {
            return LoadBalancerType.ROUND_ROBIN;
        } else if ("consistent-hash".equals(property)) {
            return LoadBalancerType.CONSISTENT_HASH;
        } else if ("weight-random".equals(property)) {
            return LoadBalancerType.WEIGHT_RANDOM;
        } else if ("weight-round-robin".equals(property)) {
            return LoadBalancerType.WEIGHT_ROUND_ROBIN;
        } else if ("adaptive".equals(property)) {
            return LoadBalancerType.ADAPTIVE;
        } else {
            return LoadBalancerType.RANDOM;
        }
    }
}

package com.wei.loadBalance.impl;

import com.wei.loadBalance.PalmxSocketAddress;
import com.wei.loadBalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 权重随机负载均衡算法
 */
public class WeightRandomLoadBalance extends AbstractLoadBalance {

    private final Random random;

    public WeightRandomLoadBalance() {
        this.random = new Random();
    }

    @Override
    protected PalmxSocketAddress doChoose(List<PalmxSocketAddress> serverNodes, String serviceName) {
        List<PalmxSocketAddress> palmxSocketAddresses = serviceNodes.get(serviceName);
        if (null == palmxSocketAddresses) {
            serviceNodes.put(serviceName, serverNodes);
            palmxSocketAddresses = serverNodes;
        }
        int weightSum = 0;
        int totalWeight = 0;
        for (PalmxSocketAddress node : serverNodes) {
            totalWeight += node.getWeight();
        }
        int randomWeight = random.nextInt(totalWeight);
        for (PalmxSocketAddress palmxSocketAddress : palmxSocketAddresses) {
            weightSum += palmxSocketAddress.getWeight();
            if (randomWeight < weightSum) {
                return palmxSocketAddress;
            }
        }
        return serverNodes.get(0);
    }
}

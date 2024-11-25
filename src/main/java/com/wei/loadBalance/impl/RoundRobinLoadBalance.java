package com.wei.loadBalance.impl;


import com.wei.loadBalance.AbstractLoadBalance;
import com.wei.loadBalance.PalmxSocketAddress;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private final Map<String, AtomicInteger> positions = new ConcurrentHashMap<>();

    @Override
    protected PalmxSocketAddress doChoose(List<PalmxSocketAddress> socketAddressList, String serviceName) {
        if (!positions.containsKey(serviceName)) {
            positions.put(serviceName, new AtomicInteger((new Random()).nextInt(1000)));
        }

        AtomicInteger position = positions.get(serviceName);
        int pos = Math.abs(position.incrementAndGet());
        return socketAddressList.get(pos % socketAddressList.size());
    }
}

package com.wei.loadBalance.impl;

import com.wei.loadBalance.AbstractLoadBalance;
import com.wei.loadBalance.PalmxSocketAddress;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Deprecated
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected PalmxSocketAddress doChoose(List<PalmxSocketAddress> socketAddressList, String serviceName) {
        int size = socketAddressList.size();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return socketAddressList.get(random.nextInt(size));
    }
}

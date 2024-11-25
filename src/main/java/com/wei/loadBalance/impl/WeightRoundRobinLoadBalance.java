package com.wei.loadBalance.impl;

import com.wei.loadBalance.AbstractLoadBalance;
import com.wei.loadBalance.PalmxSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 权重轮训负载均衡算法
 * <a href="https://www.cnblogs.com/dennyLee2025/p/16128477.html">copyright from</a>
 */
public class WeightRoundRobinLoadBalance extends AbstractLoadBalance {

    @Override
    protected PalmxSocketAddress doChoose(List<PalmxSocketAddress> socketAddressList, String serviceName) {

        List<PalmxSocketAddress> palmxSocketAddresses = serviceNodes.get(serviceName);
        if (null == palmxSocketAddresses) {
            serviceNodes.put(serviceName, socketAddressList);
            palmxSocketAddresses = socketAddressList;
        }

        // 权重之和
        Integer totalWeight = 0;
        PalmxSocketAddress nodeOfMaxWeight; // 保存轮询选中的节点信息

        // 刷新节点
        super.refreshServiceNodes(socketAddressList, serviceName);

        synchronized (palmxSocketAddresses) {
            for (PalmxSocketAddress serverNode : palmxSocketAddresses) {
                totalWeight += serverNode.getEffectiveWeight();
            }
            // 选出当前权重最大的节点
            PalmxSocketAddress tempNodeOfMaxWeight = palmxSocketAddresses.get(0);
            for (PalmxSocketAddress serverNode : palmxSocketAddresses) {
                if (serverNode.isAvailable()) {
                    serverNode.onInvokeSuccess();//提权
                } else {
                    serverNode.onInvokeFault();//降权
                }
                tempNodeOfMaxWeight = tempNodeOfMaxWeight.compareTo(serverNode) > 0 ? tempNodeOfMaxWeight : serverNode;
            }
            // 必须new个新的节点实例来保存信息，否则引用指向同一个堆实例，后面的set操作将会修改节点信息
            nodeOfMaxWeight = new PalmxSocketAddress(tempNodeOfMaxWeight.getAddress(),
                    tempNodeOfMaxWeight.getPort(), tempNodeOfMaxWeight.getWeight(),
                    tempNodeOfMaxWeight.isAvailable());
            nodeOfMaxWeight.setEffectiveWeight(tempNodeOfMaxWeight.getEffectiveWeight());
            nodeOfMaxWeight.setCurrentWeight(tempNodeOfMaxWeight.getCurrentWeight());
            // 调整当前权重比：按权重（effectiveWeight）的比例进行调整，确保请求分发合理。
            tempNodeOfMaxWeight.setCurrentWeight(tempNodeOfMaxWeight.getCurrentWeight() - totalWeight);
            palmxSocketAddresses.forEach(serverNode -> serverNode.setCurrentWeight(serverNode.getCurrentWeight() + serverNode.getEffectiveWeight()));
        }
        return nodeOfMaxWeight;
    }
}


package com.wei.mq;

import com.wei.seqMq.facade.ConsumerInfo;
import com.wei.seqMq.facade.SeqMessageQueueClient;
import com.wei.seqMq.facade.SeqMessageQueueConfig;
import com.wei.seqMq.facade.SeqMessageQueueServer;
import com.wei.util.LocalIPFinder;
import com.wei.seqMq.*;
import org.junit.jupiter.api.Test;

public class MqTests {

    @Test
    public void ServerStart() throws InterruptedException {
        SeqMessageQueueConfig seqMessageQueueConfig = seqMessageQueueConfig();
        SeqMessageQueue queue = SeqMessageQueueServer.create(seqMessageQueueConfig);
        ConsumerInfo consumerInfo = seqMessageQueueConfig.getConsumerInfo();
        queue.createStreamAndConsumerGroup(consumerInfo.getStreamName(), consumerInfo.getGroupName());
        SeqMessageQueueServer.processUnread(seqMessageQueueConfig.getConsumerInfo());
        SeqMessageQueueServer.processPending(seqMessageQueueConfig.getConsumerInfo());
        Thread.sleep(100 * 1000);
        SeqMessageQueueServer.stop(queue);
    }

    @Test
    public void ClientStart() {
        SeqMessageQueueConfig seqMessageQueueConfig = seqMessageQueueConfig();
        SeqMessageQueue queue = SeqMessageQueueClient.create(seqMessageQueueConfig);
        Person person = new Person();
        person.setColor("Red");
        person.setName("huangxuwei");
        person.setPhone("18175737312");
        person.setAge(25);
        long start = 18411100111L;
        for (int i = 0; i < 100; i++) {
            person.setId(start + i);
            String send = SeqMessageQueueClient.send(person);
        }
        SeqMessageQueueClient.stop(queue);
    }


    /**
     * 广播：一个消费者，一个消费者组
     * 单播：所有消费者，一个消费者组
     */
    private static SeqMessageQueueConfig seqMessageQueueConfig() {
        SeqMessageQueueConfig queueConfig = new SeqMessageQueueConfig();
        queueConfig.setRedisUri("redis://120.76.41.234:8866");
        queueConfig.setRedisPassword("huangxuwei");
        queueConfig.setScanPackage("com.wei.mq");
        ConsumerInfo consumerInfo = new ConsumerInfo();
        consumerInfo.setStreamName("SeqMessageQueue");
        consumerInfo.setGroupName(consumerInfo.getStreamName() + LocalIPFinder.getLocalIp());
        consumerInfo.setConsumerName("SeqConsumer");
        queueConfig.setConsumerInfo(consumerInfo);
        return queueConfig;
    }
}

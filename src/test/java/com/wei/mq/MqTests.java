
package com.wei.mq;

import com.wei.seqMq.*;
import org.junit.jupiter.api.Test;

public class MqTests {

    @Test
    public void ServerStart() throws InterruptedException {
        SeqMessageQueueConfig seqMessageQueueConfig = seqMessageQueueConfig();
        SeqMessageQueue queue = SeqMessageQueueServer.create(seqMessageQueueConfig);
        SeqMessageQueueServer.processUnread(seqMessageQueueConfig.getConsumerInfo());
        SeqMessageQueueServer.processPending(seqMessageQueueConfig.getConsumerInfo());
        Thread.sleep(15 * 1000);
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
            SeqMessageQueueClient.send(person);
        }
        SeqMessageQueueClient.stop(queue);
    }

    private static SeqMessageQueueConfig seqMessageQueueConfig() {
        SeqMessageQueueConfig queueConfig = new SeqMessageQueueConfig();
        queueConfig.setRedisUri("redis://120.76.41.234:8866");
        queueConfig.setRedisPassword("huangxuwei");
        queueConfig.setScanPackage("com.wei.mq");
        ConsumerInfo consumerInfo = new ConsumerInfo();
        consumerInfo.setStreamName("SeqMessageQueue");
        consumerInfo.setGroupName("SeqGroup");
        consumerInfo.setConsumerName("SeqConsumer");
        queueConfig.setConsumerInfo(consumerInfo);
        return queueConfig;
    }
}

package com.wei.cappuccino;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamReadGroupArgs;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
public class OrderConsumer {


    RedissonClient redissonClient;

    public void init(RedissonClient redissonClient, String group, String consumer) {
        this.redissonClient = redissonClient;
    }

    public void listenTask(String queueName, String groupName, String consumerName) {
        while (true) {
            try {
                // 1：消费者，从队列未弹出消息，并推送到 pending 队列，整个过程是原子性的
                // 最多阻塞 5 秒，超过 5 秒后还没有消息，则返回 null
                try {
                    RStream<String, String> stream = redissonClient.getStream(queueName);
                    Map<StreamMessageId, Map<String, String>> map = stream.readGroup(groupName, consumerName, StreamReadGroupArgs.neverDelivered());
                    if (map == null) {
                        log.info("等待消息 ...");
                        continue;
                    }
                    for (StreamMessageId streamMessageId : map.keySet()) {
                        log.info("streamMessageId = {}, value = {}", streamMessageId, map.get(streamMessageId));
                    }
                    log.info("消费消息: {}", queueName);
                } catch (Exception e) {
                    log.error("消费异常：{}", e.getMessage());
                    continue;
                }
                // 3：消费成功，从 pending 队列删除记录，相当于确认消费
            } catch (Exception e) {
                log.error("队列监听异常：{}", e.getMessage());
                break;
            }
        }
        log.info("退出消费");
    }
}
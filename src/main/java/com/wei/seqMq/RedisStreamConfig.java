package com.wei.seqMq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

/**
 * redis stream 监听消息
 *
 * @author ZHANGYUKUN
 */
@Configuration
@Component
public class RedisStreamConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamConfig.class);

    private static final String GROUP_NAME = "myGroup";
    private static final String STREAM_NAME = "myQueue";
    private static final String CONSUMER_NAME = "myConsumer";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Bean
    public Subscription subscription(RedisConnectionFactory redisConnectionFactory) {

        StreamOperations<String, Object, Object> streamOps = stringRedisTemplate.opsForStream();

        // 处理未 Read 消息
        processReadMessages(streamOps);
        // 处理 Pending 消息
        processPendingMessages(streamOps);

        //监听容器配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(1))
                .build();
        //监听器实现
        MyStreamListener streamListener = new MyStreamListener();
        //创建监听容器
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer = StreamMessageListenerContainer
                .create(redisConnectionFactory, options);
        //groupName需要提前创建
        Subscription subscription = listenerContainer.receiveAutoAck(Consumer.from(GROUP_NAME, CONSUMER_NAME),
                StreamOffset.create(STREAM_NAME, ReadOffset.lastConsumed()),
                streamListener);
        log.info("------------------------------------------stream监听启动-----------------------------------------------------------");
        listenerContainer.start();
        return subscription;
    }

    private void processReadMessages(StreamOperations<String, Object, Object> streamOps) {
        log.info("processReadMessages...");
        List<MapRecord<String, Object, Object>> read = streamOps.read(
                Consumer.from(GROUP_NAME, CONSUMER_NAME),
                StreamReadOptions.empty().block(Duration.ofSeconds(3)), // 阻塞 3 秒
                StreamOffset.create(STREAM_NAME, ReadOffset.lastConsumed()) // 从上次消费位置开始
        );
        for (MapRecord<String, Object, Object> entries : read) {
            onProcess(entries);
        }
    }

    class MyStreamListener implements StreamListener<String, MapRecord<String, String, String>> {
        @Override
        public void onMessage(MapRecord<String, String, String> entries) {
            RecordId id = onProcess(entries);
            ack(id.getValue());
        }
    }

    private static RecordId onProcess(MapRecord entries) {
        log.info("MapRecord = {}", entries);
        return entries.getId();
    }

    /**
     * ACK 消息
     */
    private void ack(String id) {
        stringRedisTemplate.opsForStream().acknowledge(STREAM_NAME, GROUP_NAME, id);
    }

    /**
     * 处理已发送，未确认的消息
     */
    public void processPendingMessages(StreamOperations<String, Object, Object> streamOps) {
        log.info("processPendingMessages...");

        PendingMessages pendingMessages = streamOps.pending(
                STREAM_NAME, Consumer.from(GROUP_NAME, CONSUMER_NAME));

        Iterator<PendingMessage> iterator = pendingMessages.stream().iterator();

        while (iterator.hasNext()) {
            PendingMessage pendingMessage = iterator.next();
            String messageId = pendingMessage.getId().getValue();
            String consumer = pendingMessage.getConsumer().getName();
            if (!consumer.equals(CONSUMER_NAME)) {
                log.info("PendingMessage consumer: {}", consumer);
                continue;
            }
            // 处理每个 Pending 消息
            List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().range(
                    STREAM_NAME, Range.just(messageId)
            );
            if (!records.isEmpty()) {
                MapRecord<String, Object, Object> record = records.get(0);
                onProcess(record);
            }
            ack(messageId);
        }
    }

    /**
     * 创建消费组
     */
    @PostConstruct
    public void createConsumerGroup() {
        try {
            Boolean hasKey = stringRedisTemplate.hasKey(STREAM_NAME);
            if (Boolean.TRUE.equals(hasKey)){
                return;
            }
            log.info("createConsumerGroup...");
            // 如果消费组不存在，则创建
            stringRedisTemplate.opsForStream().createGroup(STREAM_NAME, GROUP_NAME);
        } catch (Exception e) {
            // 如果消费组已存在，忽略错误
            if (!e.getMessage().contains("BUSYGROUP")) {
                throw e;
            }
        }
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void shutdown() {
        log.info("shutdown");
    }
}


package com.wei.seqMq;

import com.wei.cappuccino.RedissonHelper;
import com.wei.seqMq.facade.ConsumerInfo;
import com.wei.seqMq.facade.SeqMessageQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.api.stream.TrimStrategy;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class SeqMessageQueue {

    private static RedissonClient redissonClient;

    public void init(SeqMessageQueueConfig seqMessageQueueConfig) {
        Config config = new Config();
        config.setCodec(StringCodec.INSTANCE)
                .useSingleServer()
                .setAddress(seqMessageQueueConfig.getRedisUri())
                .setPassword(seqMessageQueueConfig.getRedisPassword());
        redissonClient = RedissonHelper.init(config);
    }

    // 创建流和消费者组，如果它们还不存在
    public void createStreamAndConsumerGroup(String streamName, String groupName) {
        RStream<String, String> stream = redissonClient.getStream(streamName);
        try {
            // 尝试创建消费者组，如果流不存在，它将被自动创建
            stream.createGroup(groupName);
        } catch (Exception e) {
            // 如果消费者组已经存在，我们将得到一个错误
            if (e.getMessage().contains("BUSYGROUP Consumer Group name already exists")) {
                log.info("Consumer group '{}' already exists for stream '{}'", groupName, streamName);
            } else {
                // 如果是其他错误，记录日志
                log.error("Error creating consumer group '{}' for stream '{}'", groupName, streamName, e);
            }
        }
    }

    // 添加事件到流
    public StreamMessageId addEventToStream(String streamName, Map<String, Object> event) {
        try {
            RStream<String, Object> stream = redissonClient.getStream(streamName);
            return stream.add(StreamAddArgs.entries(event).trim(TrimStrategy.MAXLEN, 1000));
        } catch (Exception e) {
            log.error("Error adding event to stream", e);
            return null;
        }
    }

    public static RStream<String, String> getStream(ConsumerInfo consumerInfo) {
        return redissonClient.getStream(consumerInfo.getStreamName());
    }

    // 读取事件
    public static Map<StreamMessageId, Map<String, String>> readEventsFromStream(ConsumerInfo consumerInfo) {
        try {
            RStream<String, String> stream = redissonClient.getStream(consumerInfo.getStreamName());
            return stream.readGroup(consumerInfo.getGroupName(),
                    consumerInfo.getConsumerName(),
                    StreamReadGroupArgs.neverDelivered());
        } catch (Exception e) {
            log.error("Error reading events from stream", e);
            return null;
        }
    }

    public static Map<StreamMessageId, Map<String, String>> readEventsFromStreamTtl(ConsumerInfo consumerInfo, Duration ttl) {
        try {
            RStream<String, String> stream = redissonClient.getStream(consumerInfo.getStreamName());
            return stream.readGroup(consumerInfo.getGroupName(),
                    consumerInfo.getConsumerName(),
                    StreamReadGroupArgs.neverDelivered().timeout(ttl));
        } catch (RedissonShutdownException e) {
            log.info("Redisson is shutdown msg = {}", e.getMessage());
        } catch (Throwable e) {
            log.error("listen throw exception cause = {}", e.getMessage(), e.getCause());
        }
        return null;
    }

    // 确认事件处理完成
    public static void acknowledgeEvent(ConsumerInfo consumerInfo, StreamMessageId messageId) {
        try {
            log.debug("ack message stream = {}, group = {}, id = {}", consumerInfo.getStreamName(), consumerInfo.getGroupName(), messageId);
            RStream<String, String> stream = redissonClient.getStream(consumerInfo.getStreamName());
            stream.ack(consumerInfo.getGroupName(), messageId);
        } catch (Exception e) {
            log.error("Error acknowledging event", e);
        }
    }

    // 读取并自动确认事件
    public Map<StreamMessageId, Map<String, String>> readAndAcknowledgeEventsFromStream(ConsumerInfo consumerInfo) {
        Map<StreamMessageId, Map<String, String>> messages = readEventsFromStream(consumerInfo);
        if (messages != null) {
            messages.keySet().forEach(messageId -> acknowledgeEvent(consumerInfo, messageId));
        }
        return messages;
    }

    // 允许自定义 StreamAddArgs
    public StreamMessageId addEventToStreams(String streamName, Map<String, String> event) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            // 创建 StreamAddArgs 实例并设置条目
            StreamAddArgs<String, String> streamAddArgs = StreamAddArgs.entries(event);
            // 添加事件到流
            return stream.add(streamAddArgs);
        } catch (Exception e) {
            log.error("Error adding event to stream", e);
            return null;
        }
    }

    // 允许自定义 StreamReadGroupArgs
    public Map<StreamMessageId, Map<String, String>> readEventsFromStream(String streamName, String
            groupName, String consumerName, StreamReadGroupArgs readGroupArgs) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            return stream.readGroup(groupName, consumerName, readGroupArgs);
        } catch (Exception e) {
            log.error("Error reading events from stream with custom StreamReadGroupArgs", e);
            return null;
        }
    }

    public void shutdown() {
        try {
            redissonClient.shutdown();
        } catch (Throwable e) {
            log.error("redis client stop err case = {}", e.getMessage());
        }
    }
}



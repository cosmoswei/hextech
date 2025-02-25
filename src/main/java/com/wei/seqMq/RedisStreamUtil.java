package com.wei.seqMq;

import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.api.stream.TrimStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @Author derek_smart
 * @Date 2024/7/24 8:30
 * @Description RStream 工具类
 */
public class RedisStreamUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisStreamUtil.class);

    private RedissonClient redissonClient;

    // 创建流和消费者组，如果它们还不存在
    public void createStreamAndConsumerGroup(String streamName, String groupName) {
        RStream<String, String> stream = redissonClient.getStream(streamName);
        try {
            // 尝试创建消费者组，如果流不存在，它将被自动创建
            stream.createGroup(groupName);
        } catch (Exception e) {
            // 如果消费者组已经存在，我们将得到一个错误
            if (e.getMessage().contains("BUSYGROUP Consumer Group name already exists")) {
                logger.info("Consumer group '{}' already exists for stream '{}'", groupName, streamName);
            } else {
                // 如果是其他错误，记录日志
                logger.error("Error creating consumer group '{}' for stream '{}'", groupName, streamName, e);
            }
        }
    }


    // 添加事件到流
    public StreamMessageId addEventToStream(String streamName, Map<String, String> event) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            return stream.add(StreamAddArgs.entries(event).trim(TrimStrategy.MAXLEN, 1000));
        } catch (Exception e) {
            logger.error("Error adding event to stream", e);
            return null;
        }
    }

    // 读取事件
    public Map<StreamMessageId, Map<String, String>> readEventsFromStream(String streamName, String groupName, String consumerName) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            return stream.readGroup(groupName, consumerName, StreamReadGroupArgs.neverDelivered());
        } catch (Exception e) {
            logger.error("Error reading events from stream", e);
            return null;
        }
    }

    // 确认事件处理完成
    public void acknowledgeEvent(String streamName, String groupName, StreamMessageId messageId) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            stream.ack(groupName, messageId);
        } catch (Exception e) {
            logger.error("Error acknowledging event", e);
        }
    }


    // 读取并自动确认事件
    public Map<StreamMessageId, Map<String, String>> readAndAcknowledgeEventsFromStream(String streamName, String groupName, String consumerName) {
        Map<StreamMessageId, Map<String, String>> messages = readEventsFromStream(streamName, groupName, consumerName);
        if (messages != null) {
            messages.keySet().forEach(messageId -> acknowledgeEvent(streamName, groupName, messageId));
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
            logger.error("Error adding event to stream", e);
            return null;
        }
    }

    // 允许自定义 StreamReadGroupArgs
    public Map<StreamMessageId, Map<String, String>> readEventsFromStream(String streamName, String groupName, String consumerName, StreamReadGroupArgs readGroupArgs) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            return stream.readGroup(groupName, consumerName, readGroupArgs);
        } catch (Exception e) {
            logger.error("Error reading events from stream with custom StreamReadGroupArgs", e);
            return null;
        }
    }
}

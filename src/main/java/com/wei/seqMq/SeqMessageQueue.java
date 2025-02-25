package com.wei.seqMq;

import com.wei.cappuccino.RedissonHelper;
import org.redisson.api.*;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.api.stream.TrimStrategy;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SeqMessageQueue {

    private static final Logger logger = LoggerFactory.getLogger(SeqMessageQueue.class);

    private static final String STREAM_NAME = "myQueue";
    private static final String GROUP_NAME = "myGroup";
    private static final String CONSUMER_NAME = "myConsumer";

    private static RedissonClient redissonClient;

    public void startListen(String streamKey) {
        redissonClient = RedissonHelper.get();
        RStream<Object, Object> stream = redissonClient.getStream(streamKey);
        // Get the stream object

        // Start a listener that processes incoming messages from the stream
        stream.addListener((DeletedObjectListener) s -> {
            // Message is a Map of key-value pairs
            System.out.println("Received message: " + s);
        });

    }


    public void processRead(String streamKey) {

        // Get the stream object
        RStream<String, String> stream = redissonClient.getStream("myStream");

        // XREAD to fetch messages that haven't been read by a consumer
        // Use a consumer group (ensure that the stream has a consumer group created before)
        Map<StreamMessageId, Map<String, String>> message;
        while ((message = stream.readGroup("myGroup", "myConsumer", 1)) != null) {
            System.out.println("Received unread message: " + message);
            // Process the message and acknowledge it
        }

    }

    public void processPending(String streamKey) {
        // Get the stream object
        RStream<String, String> stream = redissonClient.getStream(streamKey);

        // Get the pending messages for a specific consumer group
        String groupName = "SeqGroup";
        PendingResult pendingResult = stream.getPendingInfo(groupName);
        // Retrieve the lowest and highest message IDs
        StreamMessageId lowestId = pendingResult.getLowestId();
        StreamMessageId highestId = pendingResult.getHighestId();
        if (lowestId == null || highestId == null) {
            return;
        }
        Map<StreamMessageId, Map<String, String>> range = stream.range(lowestId, highestId);
        for (StreamMessageId streamMessageId : range.keySet()) {
            Map<String, String> stringStringMap = range.get(streamMessageId);
            System.out.println("stringStringMap = " + stringStringMap);
            acknowledgeEvent(streamKey, groupName, streamMessageId);
        }
    }

    public static void main(String[] args) {
        Config config = new Config();
        config.setCodec(StringCodec.INSTANCE).useSingleServer().setAddress("redis://120.76.41.234:8866")
                .setPassword("huangxuwei");
        redissonClient = RedissonHelper.create(config);
        SeqMessageQueue queue = new SeqMessageQueue();
        String streamKey = "SeqMessageQueue";
        String streamGroup = "SeqGroup";
        queue.createStreamAndConsumerGroup(streamKey, streamGroup);
        queue.startListen(streamKey);
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("name", "huangxuwei");
        queue.processPending(streamKey);
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



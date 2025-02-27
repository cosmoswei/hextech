package com.wei.seqMq;

import com.wei.cappuccino.RedissonHelper;
import org.redisson.api.PendingResult;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.api.stream.TrimStrategy;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeqMessageQueue {

    private static final Logger log = LoggerFactory.getLogger(SeqMessageQueue.class);

    private static RedissonClient redissonClient;

    private static final ExecutorService MQ_CONSUMER_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private boolean listenStop = false;

    public void processPending(String streamKey, String groupName) {
        // Get the stream object
        RStream<String, String> stream = redissonClient.getStream(streamKey);

        // Get the pending messages for a specific consumer group
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
            log.info("{} value = {}", streamMessageId, stringStringMap);

            acknowledgeEvent(streamKey, groupName, streamMessageId);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.setCodec(StringCodec.INSTANCE)
                .useSingleServer()
                .setAddress("redis://120.76.41.234:8866")
                .setPassword("huangxuwei");
        redissonClient = RedissonHelper.init(config);
        SeqMessageQueue queue = new SeqMessageQueue();
        String streamKey = "SeqMessageQueue";
        String streamGroup = "SeqGroup";
        String streamConsumer = "SeqConsumer";
        queue.createStreamAndConsumerGroup(streamKey, streamGroup);
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("key", "cosmoswei");
        map.put("id", "18411100111");
        map.put("age", "25");
        map.put("name", "huangxuwei");
        map.put("phone", "18175737312");
        map.put("color", "red");
        StreamMessageId streamId = queue.addEventToStream(streamKey, map);
        Map<StreamMessageId, Map<String, String>> streamMessageIdMap = queue.readEventsFromStream(streamKey, streamGroup, streamConsumer);
        for (StreamMessageId streamMessageId : streamMessageIdMap.keySet()) {
            log.info("streamMessageId = {}, value = {}", streamMessageId, streamMessageIdMap.get(streamMessageId));
            queue.acknowledgeEvent(streamKey, streamGroup, streamMessageId);
        }
        queue.processPending(streamKey, streamGroup);

        MQ_CONSUMER_POOL.execute(() -> queue.listen(streamKey, streamGroup, streamConsumer));

        Thread.sleep(60 * 1000);

        queue.shutdown();
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
    public StreamMessageId addEventToStream(String streamName, Map<String, String> event) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            return stream.add(StreamAddArgs.entries(event).trim(TrimStrategy.MAXLEN, 1000));
        } catch (Exception e) {
            log.error("Error adding event to stream", e);
            return null;
        }
    }

    // 读取事件
    public Map<StreamMessageId, Map<String, String>> readEventsFromStream(String streamName, String groupName, String consumerName) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            return stream.readGroup(groupName, consumerName, StreamReadGroupArgs.neverDelivered());
        } catch (Exception e) {
            log.error("Error reading events from stream", e);
            return null;
        }
    }

    public Map<StreamMessageId, Map<String, String>> readEventsFromStreamTtl(String streamName, String groupName, String consumerName, Duration ttl) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);

            return stream.readGroup(groupName, consumerName, StreamReadGroupArgs.neverDelivered().timeout(ttl));
        } catch (Exception e) {
            log.error("Error reading events from stream", e);
            return null;
        }
    }

    public void listen(String streamName, String groupName, String consumerName) {

        log.info("start listen stream message");
        while (!listenStop) {
            Map<StreamMessageId, Map<String, String>> streamMessageIdMapMap = readEventsFromStreamTtl(streamName, groupName, consumerName, Duration.ofSeconds(5));
            for (StreamMessageId streamMessageId : streamMessageIdMapMap.keySet()) {
                Map<String, String> stringMap = streamMessageIdMapMap.get(streamMessageId);
                if (stringMap == null) {
                    continue;
                }
                handlerListen(streamName, groupName, streamMessageId, stringMap);
            }
        }
    }

    public void handlerListen(String streamName, String groupName,
                              StreamMessageId messageId, Map<String, String> data) {
        System.out.println("data = " + data);
        acknowledgeEvent(streamName, groupName, messageId);
    }


    // 确认事件处理完成
    public void acknowledgeEvent(String streamName, String groupName, StreamMessageId messageId) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            stream.ack(groupName, messageId);
        } catch (Exception e) {
            log.error("Error acknowledging event", e);
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
            log.error("Error adding event to stream", e);
            return null;
        }
    }

    // 允许自定义 StreamReadGroupArgs
    public Map<StreamMessageId, Map<String, String>> readEventsFromStream(String streamName, String groupName, String consumerName, StreamReadGroupArgs readGroupArgs) {
        try {
            RStream<String, String> stream = redissonClient.getStream(streamName);
            return stream.readGroup(groupName, consumerName, readGroupArgs);
        } catch (Exception e) {
            log.error("Error reading events from stream with custom StreamReadGroupArgs", e);
            return null;
        }
    }

    public void shutdown() {
        this.listenStop = true;
        redissonClient.shutdown();
        MQ_CONSUMER_POOL.shutdown();
    }

}



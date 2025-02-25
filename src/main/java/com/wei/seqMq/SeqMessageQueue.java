package com.wei.seqMq;

import com.wei.cappuccino.RedissonHelper;
import org.redisson.api.*;
import org.redisson.api.stream.StreamReadParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SeqMessageQueue {

    private static final Logger log = LoggerFactory.getLogger(SeqMessageQueue.class);

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
        String groupName = "myGroup";
        PendingResult pendingResult = stream.getPendingInfo(groupName);
        // Retrieve the lowest and highest message IDs
        StreamMessageId lowestId = pendingResult.getLowestId();
        StreamMessageId highestId = pendingResult.getHighestId();
        List<PendingEntry> pendingEntries = stream.listPending(STREAM_NAME, lowestId, highestId, 100);

    }
}


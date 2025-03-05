package com.wei.seqMq.facade;

import com.wei.seqMq.SeqMessageQueue;
import com.wei.seqMq.StreamMessageListenerContainer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.PendingResult;
import org.redisson.api.RStream;
import org.redisson.api.StreamMessageId;

import java.util.Map;

@Data
@Slf4j
public class SeqMessageQueueServer {
    private static StreamMessageListenerContainer streamMessageListenerContainer;
    private SeqMessageQueueConfig config;
    private static SeqMessageQueue seqMessageQueue;
    private static ConsumerInfo consumerInfo;

    public static SeqMessageQueue create(SeqMessageQueueConfig seqMessageQueueConfig) {
        SeqMessageQueue queue = new SeqMessageQueue();
        queue.init(seqMessageQueueConfig);
        consumerInfo = seqMessageQueueConfig.getConsumerInfo();
        seqMessageQueue = queue;
        streamMessageListenerContainer = StreamMessageListenerContainer.create()
                .scan(seqMessageQueueConfig.getScanPackage());
        streamMessageListenerContainer.start();
        return queue;
    }

    public static void stop(SeqMessageQueue queue) {
        streamMessageListenerContainer.stop();
        queue.shutdown();
    }

    public static void processUnread(ConsumerInfo consumerInfo) {
        log.info("processUnread...");
        Map<StreamMessageId, Map<String, String>> streamMessageIdMap = SeqMessageQueue.readEventsFromStream(consumerInfo);

        if (null == streamMessageIdMap) {
            return;
        }
        for (StreamMessageId streamMessageId : streamMessageIdMap.keySet()) {
            log.info("streamMessageId = {}, value = {}", streamMessageId, streamMessageIdMap.get(streamMessageId));
            SeqMessageQueue.acknowledgeEvent(consumerInfo, streamMessageId);
        }
    }

    public static void processPending(ConsumerInfo consumerInfo) {
        log.info("processPending...");
        // Get the stream object
        RStream<String, String> stream = SeqMessageQueue.getStream(consumerInfo);
        // Get the pending messages for a specific consumer group
        PendingResult pendingResult = stream.getPendingInfo(consumerInfo.getGroupName());
        // Retrieve the lowest and highest message IDs
        StreamMessageId lowestId = pendingResult.getLowestId();
        StreamMessageId highestId = pendingResult.getHighestId();
        if (lowestId == null || highestId == null) {
            return;
        }
        Map<StreamMessageId, Map<String, String>> range = stream.range(lowestId, highestId);
        for (StreamMessageId streamMessageId : range.keySet()) {
            Map<String, String> stringStringMap = range.get(streamMessageId);
            log.debug("{} value = {}", streamMessageId, stringStringMap);
            SeqMessageQueue.acknowledgeEvent(consumerInfo, streamMessageId);
        }
    }

}

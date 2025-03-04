package com.wei.seqMq;

import com.wei.json.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.StreamMessageId;

import java.util.Map;

@Data
@Slf4j
public class SeqMessageQueueClient {
    private SeqMessageQueueConfig config;
    private static SeqMessageQueue seqMessageQueue;
    private static ConsumerInfo consumerInfo;

    public static SeqMessageQueue create(SeqMessageQueueConfig seqMessageQueueConfig) {
        SeqMessageQueue queue = new SeqMessageQueue();
        queue.init(seqMessageQueueConfig);
        seqMessageQueue = queue;
        consumerInfo = seqMessageQueueConfig.getConsumerInfo();
        return queue;
    }

    public static void stop(SeqMessageQueue queue) {
        queue.shutdown();
    }

    public static String send(Object o) {
        Map<String, Object> stringObjectMap = JsonUtil.objectToMap(o);
        StreamMessageId streamMessageId = seqMessageQueue.addEventToStream(consumerInfo.getStreamName(), stringObjectMap);
        return streamMessageId.toString();
    }

}

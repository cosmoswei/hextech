package com.wei.seqMq;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.StreamMessageId;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class ListenerTask implements Runnable {

    private final ConsumerInfo consumerInfo;

    private boolean listenStop = false;

    public ListenerTask(ConsumerInfo consumerInfo) {
        this.consumerInfo = consumerInfo;
    }

    @Override
    public void run() {
        listen();
    }

    public void listen() {
        log.info("start listen stream message");
        while (!listenStop) {
            Map<StreamMessageId, Map<String, String>> streamMessageIdMapMap = readEventsFromStreamTtl(consumerInfo, Duration.ofSeconds(5));
            for (StreamMessageId streamMessageId : streamMessageIdMapMap.keySet()) {
                Map<String, String> stringMap = streamMessageIdMapMap.get(streamMessageId);
                if (stringMap == null) {
                    continue;
                }
                handlerListen(consumerInfo, streamMessageId, stringMap);
            }
        }
    }

    private Map<StreamMessageId, Map<String, String>> readEventsFromStreamTtl(ConsumerInfo consumerInfo, Duration duration) {
        return null;
    }

    public void handlerListen(ConsumerInfo consumerInfo,
                              StreamMessageId messageId, Map<String, String> data) {
        System.out.println("messageId= " + messageId + ", data = " + data);
    }
}

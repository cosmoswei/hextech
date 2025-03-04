package com.wei.seqMq;

import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonShutdownException;
import org.redisson.api.StreamMessageId;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class ListenerTask implements Runnable {

    private final ConsumerInfo consumerInfo;

    private final StreamListener streamListener;

    private boolean isActive = false;

    public boolean isActive() {
        return this.isActive;
    }

    public void active() {
        this.isActive = true;
    }

    public void stop() {
        this.isActive = false;
    }

    public ListenerTask(ConsumerInfo consumerInfo, StreamListener streamListener) {
        this.consumerInfo = consumerInfo;
        this.streamListener = streamListener;
    }

    @Override
    public void run() {
        listen();
    }

    public void listen() {
        log.info("start listen stream message...");
        while (true) {
            if (!canLoop()) {
                continue;
            }
            try {
                Thread.sleep(0);
                Map<StreamMessageId, Map<String, String>> streamMessageIdMapMap = readEventsFromStreamTtl(consumerInfo, Duration.ofSeconds(10));
                log.debug("listen msg = {}", streamMessageIdMapMap);
                if (null == streamMessageIdMapMap) {
                    continue;
                }
                for (StreamMessageId streamMessageId : streamMessageIdMapMap.keySet()) {
                    Map<String, String> stringMap = streamMessageIdMapMap.get(streamMessageId);
                    if (stringMap == null) {
                        continue;
                    }
                    handlerListen(consumerInfo, streamMessageId, stringMap);
                }
            } catch (RedissonShutdownException e) {
                log.info("Redisson is shutdown msg = {}", e.getMessage());
            } catch (Throwable e) {
                log.error("listen throw exception cause = {}", e.getMessage(), e.getCause());
            }
        }
    }

    private boolean canLoop() {
        return isActive;
    }

    private Map<StreamMessageId, Map<String, String>> readEventsFromStreamTtl(ConsumerInfo consumerInfo, Duration duration) {
        return SeqMessageQueue.readEventsFromStreamTtl(consumerInfo,
                duration);
    }

    public void handlerListen(ConsumerInfo consumerInfo,
                              StreamMessageId messageId, Map<String, String> data) {
        SeqMessage seqMessage = new SeqMessage();
        seqMessage.setSeqMessageId(String.valueOf(messageId.getId0()));
        seqMessage.setSeqMessageData(data);
        streamListener.onMessage(seqMessage);
        if (streamListener.autoAck()) {
            SeqMessageQueue.acknowledgeEvent(consumerInfo, messageId);
        }
    }
}

package com.wei.mq;

import com.wei.seqMq.SeqMessage;
import com.wei.seqMq.StreamConsumer;
import com.wei.seqMq.StreamListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@StreamConsumer(
        streamName = "SeqMessageQueue",
        groupName = "SeqGroup"
)
public class MyStreamListener implements StreamListener<SeqMessage> {

    private boolean actAck = true;

    @Override
    public void onMessage(SeqMessage message) {
        log.info("message = {}", message);
    }

    @Override
    public boolean autoAck() {
        return actAck;
    }

    public void setActAck(boolean actAck) {
        this.actAck = actAck;
    }
}
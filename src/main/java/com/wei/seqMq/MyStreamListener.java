package com.wei.seqMq;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@StreamConsumer(
        groupName = "SeqMessageQueue",
        streamName = "SeqGroup",
        autoAck = true
)
class MyStreamListener implements StreamListener<SeqMessage> {

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
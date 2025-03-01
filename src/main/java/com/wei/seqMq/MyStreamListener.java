package com.wei.seqMq;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class MyStreamListener implements StreamListener<SeqMessage> {

    boolean actAck = true;

    @Override
    public void onMessage(SeqMessage message) {
        log.info("message = {}", message);
    }

    @Override
    public boolean autoAck() {
        return actAck;
    }
}
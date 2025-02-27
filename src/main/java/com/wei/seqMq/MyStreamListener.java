package com.wei.seqMq;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class MyStreamListener implements StreamListener<SeqMessage> {

    @Override
    public void onMessage(SeqMessage message) {
        log.info("message = {}", message);
    }
}
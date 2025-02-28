package com.wei.seqMq;

public interface StreamMessageListenerContainer<SeqMessage> {
    void register(ConsumerInfo consumerInfo);
}

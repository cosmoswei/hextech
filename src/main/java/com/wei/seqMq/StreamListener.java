package com.wei.seqMq;

public interface StreamListener<V extends SeqMessage> {
    void onMessage(V message);

    boolean autoAck();
}
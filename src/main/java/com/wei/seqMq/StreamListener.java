package com.wei.seqMq;

@FunctionalInterface
public interface StreamListener<V extends SeqMessage> {
    void onMessage(V message);
}
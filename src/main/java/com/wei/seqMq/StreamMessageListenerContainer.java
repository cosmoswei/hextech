package com.wei.seqMq;

public interface StreamMessageListenerContainer {

    static StreamMessageListenerContainer create() {
        return new DefaultStreamMessageListenerContainer();
    }

    void register(ConsumerInfo consumerInfo, StreamListener streamListener);

    StreamMessageListenerContainer scan(String path);

    void start();

    void receive(ConsumerInfo consumerInfo, StreamListener streamListener, boolean autoAck);

    default void receive(ConsumerInfo consumerInfo, StreamListener streamListener) {
        this.receive(consumerInfo, streamListener, false);
    }

    default void receiveAutoAck(ConsumerInfo consumerInfo, StreamListener streamListener) {
        this.receive(consumerInfo, streamListener, true);
    }
}

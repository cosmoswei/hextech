package com.wei.seqMq;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class DefaultStreamMessageListenerContainer implements StreamMessageListenerContainer<SeqMessage> {

    private static final ExecutorService MQ_CONSUMER_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    List<ListenerTask> subscriptions = new ArrayList<>();

    public void receive(String stream, String group, String consumer, StreamListener streamListener) {
    }

    public void receiveAutoAck() {
    }

    public void start() {
    }

    @Override
    public void register(ConsumerInfo consumerInfo) {
        this.doRegister(consumerInfo);
    }

    private void doRegister(ConsumerInfo consumerInfo) {
        ListenerTask listenerTask = new ListenerTask(consumerInfo);
        subscriptions.add(listenerTask);
        new ListenerTask(null);
        MQ_CONSUMER_POOL.execute(listenerTask);
    }
}
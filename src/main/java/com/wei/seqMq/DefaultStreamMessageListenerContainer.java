package com.wei.seqMq;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class DefaultStreamMessageListenerContainer implements StreamMessageListenerContainer {

    private static final ExecutorService MQ_CONSUMER_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private boolean running = false;

    List<ListenerTask> subscriptions = new ArrayList<>();

    public void start() {
    }

    @Override
    public void receive(ConsumerInfo consumerInfo, StreamListener streamListener, boolean autoAck) {
        this.doRegister(consumerInfo, streamListener);
    }

    @Override
    public void register(ConsumerInfo consumerInfo, StreamListener streamListener) {
        this.doRegister(consumerInfo, streamListener);
    }

    private void doRegister(ConsumerInfo consumerInfo, StreamListener streamListener) {
        ListenerTask listenerTask = new ListenerTask(consumerInfo, streamListener);
        subscriptions.add(listenerTask);
        if (canRun()) {
            MQ_CONSUMER_POOL.execute(listenerTask);
        }

    }

    private boolean canRun() {
        return true;
    }
}
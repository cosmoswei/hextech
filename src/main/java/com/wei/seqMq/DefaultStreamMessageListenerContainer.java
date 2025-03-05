package com.wei.seqMq;


import com.wei.seqMq.facade.ConsumerInfo;
import com.wei.seqMq.aop.StreamConsumerRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class DefaultStreamMessageListenerContainer implements StreamMessageListenerContainer {

    private static final ExecutorService MQ_CONSUMER_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    List<ListenerTask> subscriptions = new ArrayList<>();

    private boolean running = false;

    @Override
    public void start() {
        this.running = true;
        for (ListenerTask subscription : subscriptions) {
            if (subscription.isActive()) {
                continue;
            }
            subscription.active();
            MQ_CONSUMER_POOL.execute(subscription);
        }
    }

    @Override
    public void stop() {
        this.running = false;
        for (ListenerTask subscription : subscriptions) {
            subscription.stop();
        }
        MQ_CONSUMER_POOL.shutdown();
    }

    @Override
    public void receive(ConsumerInfo consumerInfo, StreamListener streamListener, boolean autoAck) {
        this.doRegister(consumerInfo, streamListener);
    }

    @Override
    public void register(ConsumerInfo consumerInfo, StreamListener streamListener) {
        this.doRegister(consumerInfo, streamListener);
    }

    @Override
    public StreamMessageListenerContainer scan(String path) {
        StreamConsumerRegistrar.register(this, path);
        return this;
    }

    private void doRegister(ConsumerInfo consumerInfo, StreamListener streamListener) {
        ListenerTask listenerTask = new ListenerTask(consumerInfo, streamListener);
        subscriptions.add(listenerTask);
        if (canRun()) {
            MQ_CONSUMER_POOL.execute(listenerTask);
        }

    }

    private boolean canRun() {
        return running;
    }
}
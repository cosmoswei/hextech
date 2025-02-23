package com.wei.cappuccino;

public class RedisStreamNotify implements MessageNotify{
    @Override
    public void listen(NotifyMsg msg) {

    }

    @Override
    public boolean broadcast(NotifyMsg msg) {
        return false;
    }
}

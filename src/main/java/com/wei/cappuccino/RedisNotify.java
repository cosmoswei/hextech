package com.wei.cappuccino;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisNotify implements MessageNotify {

    private static final Logger log = LoggerFactory.getLogger(RedisNotify.class);

    @Override
    public void listen(NotifyMsg msg) {

    }

    @Override
    public boolean broadcast(NotifyMsg msg) {
        log.info("broadcast a msg = {}", msg);
        return false;
    }
}

package com.wei.cappuccino;

public interface MessageNotify {
    void listen(NotifyMsg msg);

    boolean broadcast(NotifyMsg msg);
}

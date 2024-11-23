package com.wei.flowControl.core;

public interface FlowControl {

    FlowControl init(int count, int interval);

    boolean canPass();

}

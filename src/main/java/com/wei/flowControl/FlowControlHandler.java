package com.wei.flowControl;

import com.wei.flowControl.constant.FlowControlConstant;
import com.wei.flowControl.core.FlowControl;
import com.wei.flowControl.core.impl.CounterFlowControl;
import com.wei.flowControl.core.impl.LeakBucketFlowControl;
import com.wei.flowControl.core.impl.SlidingWindowFlowControl;
import com.wei.flowControl.core.impl.TokenBucketFlowControl;
import com.wei.flowControl.exception.FlowControlException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FlowControlHandler {

    private FlowControlHandler() {
    }

    private static final Map<String, FlowControl> flowControlMap = new ConcurrentHashMap<>();

    public static void init(InitMetaData initMetaData) {
        if (initMetaData == null) {
            throw new FlowControlException("initResource cant be null");
        }
        String resourceKey = initMetaData.getResourceKey();
        int count = initMetaData.getCount();
        String flowControlType = initMetaData.getFlowControlType();
        int interval = initMetaData.getInterval();
        FlowControl flowControlInstance = getFlowControlObj(flowControlType);
        flowControlInstance = flowControlInstance.init(count, interval);
        flowControlMap.putIfAbsent(resourceKey, flowControlInstance);
        log.info("init {{}} flow control success", resourceKey);
    }

    private static FlowControl getFlowControlObj(String flowControlType) {
        switch (flowControlType) {
            case FlowControlConstant.COUNTER:
                return CounterFlowControl.newCounterFlowControl();
            case FlowControlConstant.SLIDING_WINDOW:
                return SlidingWindowFlowControl.newSlidingWindowFlowControl();
            case FlowControlConstant.LEAKY_BUCKET:
                return LeakBucketFlowControl.newLeakBucketFlowControl();
            case FlowControlConstant.TOKEN_BUCKET:
                return TokenBucketFlowControl.newTokenBucketFlowControl();
        }
        // 默认返回计数器
        return CounterFlowControl.newCounterFlowControl();
    }

    public static boolean canPass(String resource) {
        // 根据接口/资源找到对应的流控器
        FlowControl flowControl = flowControlMap.get(resource);
        if (flowControl == null) {
            log.error("flowControl error, flowControl not init");
            return true;
        }
        try {
            return flowControl.canPass();
        } catch (Exception e) {
            log.error("flowControl canPass error", e);
            return true;
        }
    }
}

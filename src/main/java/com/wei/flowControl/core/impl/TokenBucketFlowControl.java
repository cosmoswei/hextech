package com.wei.flowControl.core.impl;


import com.wei.flowControl.core.AbstractFlowControl;
import com.wei.flowControl.core.FlowControl;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TokenBucketFlowControl extends AbstractFlowControl {

    private long lastTime = System.currentTimeMillis();

    // 桶的容量
    public int capacity;
    // 令牌生成速度 /s
    public int rate;
    // 当前令牌数量
    public AtomicInteger tokens;

    @Override
    public FlowControl init(int count, int interval) {
        FlowControl flowControl = super.init(count, interval);
        int qps = count / interval;
        capacity = qps;
        rate = qps;
        tokens = new AtomicInteger(capacity);
        return flowControl;
    }

    @Override
    public boolean canPass() {

        //
        capacity = getQps();

        long nowTime = System.currentTimeMillis();
        long diff = nowTime - lastTime;

        //计算时间段内的令牌数
        int innerTokens = (int) (diff * rate / 1000);
        int all = tokens.get() + innerTokens;

        // 剩余令牌
        tokens.set(Math.min(capacity, all));

        log.info("tokens = {}, capacity = {}", tokens.get(), capacity);

        // 没有令牌
        if (tokens.get() <= 0) {
            return false;
        } else {
            // 还有令牌
            tokens.decrementAndGet();
            lastTime = nowTime;
            return true;
        }
    }

    public boolean canPass2() {
        long nowTime = System.currentTimeMillis();

        // 原子性地计算新的令牌数并更新剩余令牌
        tokens.updateAndGet(currentTokens -> {
            long diff = nowTime - lastTime;
            lastTime = nowTime;

            // 根据时间差计算新的令牌数
            int addedTokens = (int) (diff * rate / 1000);
            int totalTokens = Math.min(capacity, currentTokens + addedTokens);

            // 返回更新后的令牌数
            return totalTokens;
        });

        // 检查令牌数是否足够
        if (tokens.get() <= 0) {
            return false;
        }

        // 减少令牌数
        tokens.decrementAndGet();
        return true;
    }

    public static TokenBucketFlowControl newTokenBucketFlowControl() {
        return new TokenBucketFlowControl();
    }

}

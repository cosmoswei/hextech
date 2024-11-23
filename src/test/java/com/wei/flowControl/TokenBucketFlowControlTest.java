package com.wei.flowControl;

import com.wei.flowControl.constant.FlowControlConstant;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketFlowControlTest {

    @Test
    void testBasicFunctionality() {
        FlowControlHandler.init(new InitMetaData("testBasicFunctionality", FlowControlConstant.TOKEN_BUCKET, 5, 1));

        // 检查初始限流
        assertTrue(FlowControlHandler.canPass("testBasicFunctionality"));
        assertTrue(FlowControlHandler.canPass("testBasicFunctionality"));
        assertTrue(FlowControlHandler.canPass("testBasicFunctionality"));
        assertTrue(FlowControlHandler.canPass("testBasicFunctionality"));
        assertTrue(FlowControlHandler.canPass("testBasicFunctionality"));
        assertFalse(FlowControlHandler.canPass("testBasicFunctionality")); // 超过容量，应被限流
    }

    @Test
    void testTokenRefillOverTime() throws InterruptedException {
        FlowControlHandler.init(new InitMetaData("testTokenRefillOverTime", FlowControlConstant.TOKEN_BUCKET, 2, 1));

        // 消耗令牌
        assertTrue(FlowControlHandler.canPass("testTokenRefillOverTime"));
        assertTrue(FlowControlHandler.canPass("testTokenRefillOverTime"));
        assertFalse(FlowControlHandler.canPass("testTokenRefillOverTime")); // 无令牌，应限流

        // 等待 2 秒，令牌应该被填充
        Thread.sleep(2000);
        assertTrue(FlowControlHandler.canPass("testTokenRefillOverTime"));
        assertTrue(FlowControlHandler.canPass("testTokenRefillOverTime"));
        assertFalse(FlowControlHandler.canPass("testTokenRefillOverTime")); // 再次超限
    }

    @Test
    void testHighConcurrency() throws InterruptedException {
        FlowControlHandler.init(new InitMetaData("testTokenRefillOverTime", FlowControlConstant.TOKEN_BUCKET, 10, 5));

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        int[] passCount = {0};
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    if (FlowControlHandler.canPass("testTokenRefillOverTime")) {
                        synchronized (passCount) {
                            passCount[0]++;
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 等待所有线程完成
        executor.shutdown();

        // 确保通过的请求数不超过令牌桶容量
        assertTrue(passCount[0] <= 10, "Pass count should not exceed token bucket capacity");
    }
}
package com.wei.flowControl;

import com.wei.flowControl.constant.FlowControlConstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlidingWindowFlowControlTest {


    @Test
    public void testCanPass_singleThread() throws InterruptedException {

        // 初始化限流器
        InitMetaData initMetaData = new InitMetaData("testCanPass_singleThread", FlowControlConstant.SLIDING_WINDOW, 5, 1);
        FlowControlHandler.init(initMetaData);

        int allowedRequests = 0;
        for (int i = 0; i < 10; i++) {
            if (FlowControlHandler.canPass("testCanPass_singleThread")) {
                allowedRequests++;
            }
            Thread.sleep(50); // 模拟请求间隔
        }

        // 在 1 秒内，最多允许 5 个请求
        assertEquals(5, allowedRequests);
    }

    @Test
    public void testCanPass_multiThread() throws InterruptedException {
        // 初始化限流器
        InitMetaData initMetaData = new InitMetaData("testCanPass_multiThread", FlowControlConstant.SLIDING_WINDOW, 50, 2);
        FlowControlHandler.init(initMetaData);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger passedRequests = new AtomicInteger();

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                if (FlowControlHandler.canPass("testCanPass_multiThread")) {
                    passedRequests.incrementAndGet();
                }
            });
        }

        Thread.sleep(1000);

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                if (FlowControlHandler.canPass("testCanPass_multiThread")) {
                    passedRequests.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(10);
        }

        // 在 2 秒内，最多允许 50 个请求
        assertEquals(50, passedRequests.get());
    }

    @Test
    public void testWindowSlide() throws InterruptedException {
        // 初始化限流器
        InitMetaData initMetaData = new InitMetaData("testWindowSlide", FlowControlConstant.SLIDING_WINDOW, 2, 1);
        FlowControlHandler.init(initMetaData);

        assertTrue(FlowControlHandler.canPass("testWindowSlide"));
        assertTrue(FlowControlHandler.canPass("testWindowSlide"));
        Thread.sleep(1100); // 超过 1 秒窗口，滑动窗口刷新

        assertTrue(FlowControlHandler.canPass("testWindowSlide"));
        assertTrue(FlowControlHandler.canPass("testWindowSlide"));
    }

    @Test
    public void testSlidingWindowFlowControl() throws InterruptedException {
        // 初始化限流器
        InitMetaData initMetaData = new InitMetaData("testSlidingWindowFlowControl", FlowControlConstant.SLIDING_WINDOW, 5, 1);
        FlowControlHandler.init(initMetaData);

        // 并发测试
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger passedRequests = new AtomicInteger();
        AtomicInteger rejectedRequests = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    boolean result = FlowControlHandler.canPass("testSlidingWindowFlowControl");
                    if (result) {
                        passedRequests.incrementAndGet();
                    } else {
                        rejectedRequests.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 验证结果
        System.out.println("Passed Requests: " + passedRequests.get());
        System.out.println("Rejected Requests: " + rejectedRequests.get());

        // 滑动窗口限流，每秒最多 5 个请求
        Assertions.assertTrue(passedRequests.get() <= 5);
        Assertions.assertTrue(rejectedRequests.get() >= 45);
    }
}
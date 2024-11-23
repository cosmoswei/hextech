package com.wei.flowControl;


import com.wei.flowControl.constant.FlowControlConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;


class CounterFlowControlTest {

    @BeforeEach
    void setUp() {
        // 清理限流器状态（如果 FlowControlHandler 中有静态清理方法，可以调用）
        // 如果没有静态清理方法，需要手动清理 flowControlMap 中的内容
    }

    @Test
    void testInitFlowControl() {
        // 初始化限流器
        InitMetaData initMetaData = new InitMetaData("testResource", FlowControlConstant.COUNTER, 10, 1000);
        FlowControlHandler.init(initMetaData);
    }

    @Test
    void testCanPassWithoutInit() {
        // 对未初始化的资源进行请求，应默认允许通过并记录错误日志
        assertTrue(FlowControlHandler.canPass("testCanPassWithoutInit"), "Uninitialized resource should pass by default");
    }

    @Test
    void testCanPassWithinLimit() {
        // 初始化流控器
        InitMetaData resource = new InitMetaData("testCanPassWithinLimit", FlowControlConstant.COUNTER, 5, 1000);
        FlowControlHandler.init(resource);

        // 模拟连续请求，数量不超过上限
        for (int i = 0; i < 5; i++) {
            assertTrue(FlowControlHandler.canPass("testCanPassWithinLimit"));
        }

        // 第六次请求，超过上限，应限流
        assertFalse(FlowControlHandler.canPass("testCanPassWithinLimit"));
    }

    @Test
    void testCanPassAfterInterval() throws InterruptedException {
        // 初始化流控器
        InitMetaData resource = new InitMetaData("testCanPassAfterInterval", FlowControlConstant.COUNTER, 5, 1000);
        FlowControlHandler.init(resource);

        // 模拟超过上限的请求
        for (int i = 0; i < 5; i++) {
            assertTrue(FlowControlHandler.canPass("testCanPassAfterInterval"));
        }
        assertFalse(FlowControlHandler.canPass("testCanPassAfterInterval"));

        // 等待时间窗口重置
        Thread.sleep(1000);

        // 窗口重置后，应该可以再次请求
        assertTrue(FlowControlHandler.canPass("testCanPassAfterInterval"));
    }

    @Test
    void testCanPassConcurrentRequests() throws InterruptedException {
        // 初始化流控器
        InitMetaData resource = new InitMetaData("testCanPassConcurrentRequests", FlowControlConstant.COUNTER, 50, 1000);
        FlowControlHandler.init(resource);

        int threadCount = 100; // 模拟 100 个线程
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int[] successCount = {0};
        int[] failureCount = {0};

        // 高并发测试
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                if (FlowControlHandler.canPass("testCanPassConcurrentRequests")) {
                    synchronized (successCount) {
                        successCount[0]++;
                    }
                } else {
                    synchronized (failureCount) {
                        failureCount[0]++;
                    }
                }
                latch.countDown();
            });
        }

        latch.await(); // 等待所有线程完成
        executor.shutdown();

        System.out.println("Success count: " + successCount[0]);
        System.out.println("Failure count: " + failureCount[0]);

        // 断言：最多允许 50 次通过，其余为失败
        assertEquals(50, successCount[0]);
        assertEquals(50, failureCount[0]);
    }
}
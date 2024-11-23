package com.wei.flowControl;

import com.wei.flowControl.constant.FlowControlConstant;
import com.wei.flowControl.exception.FlowControlException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class LeakBucketFlowControlTest {

    @Test
    public void testLeakBucketFlowControlTest() throws InterruptedException {
        // 初始化滑动窗口限流配置
        InitMetaData resource = new InitMetaData();
        resource.setResourceKey("testLeakBucketFlowControlTest");
        resource.setFlowControlType(FlowControlConstant.LEAKY_BUCKET);
        resource.setCount(10); // 每秒允许10次请求
        resource.setInterval(1); // 时间窗口为1秒

        FlowControlHandler.init(resource);

        // 并发测试
        int threads = 50; // 模拟50个线程并发
        int requestsPerThread = 5; // 每个线程尝试发送5次请求
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        int[] passedRequests = {0}; // 成功通过限流的请求计数

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    if (FlowControlHandler.canPass("testLeakBucketFlowControlTest")) {
                        synchronized (passedRequests) {
                            passedRequests[0]++;
                        }
                    }
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        // 验证通过的请求总数是否在预期范围内
        // 10次/秒滑动窗口，可能存在小幅波动
        int expectedMaxPass = resource.getCount();
        assertEquals(expectedMaxPass, passedRequests[0], "通过限流的请求数不符合预期！");
    }

    @Test
    void testHighConcurrency() throws InterruptedException {
        // 初始化滑动窗口限流器
        FlowControlHandler.init(new InitMetaData("testHighConcurrency", FlowControlConstant.LEAKY_BUCKET, 100, 1));

        int totalRequests = 1000;
        int allowedRequests = 100;
        CountDownLatch latch = new CountDownLatch(totalRequests);
        ExecutorService executor = Executors.newFixedThreadPool(50);

        int[] passCount = {0};

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                if (FlowControlHandler.canPass("testHighConcurrency")) {
                    synchronized (passCount) {
                        passCount[0]++;
                    }
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(allowedRequests, passCount[0], "Allowed requests should match the configured limit.");
    }

    @Test
    void testEdgeCaseAtLimit() {
        FlowControlHandler.init(new InitMetaData("testEdgeCaseAtLimit", FlowControlConstant.LEAKY_BUCKET, 10, 1));

        for (int i = 0; i < 10; i++) {
            assertTrue(FlowControlHandler.canPass("testEdgeCaseAtLimit"), "Request should pass under the limit.");
        }

        assertFalse(FlowControlHandler.canPass("testEdgeCaseAtLimit"), "Request should fail at the limit.");
    }

    @Test
    void testExpiredRequestsCleaned() throws InterruptedException {
        FlowControlHandler.init(new InitMetaData("testExpiredRequestsCleaned", FlowControlConstant.LEAKY_BUCKET, 5, 1));

        for (int i = 0; i < 5; i++) {
            assertTrue(FlowControlHandler.canPass("resource-clean-expired"));
        }

        // Wait for requests to expire
        Thread.sleep(1500);

        assertTrue(FlowControlHandler.canPass("testExpiredRequestsCleaned"), "Expired requests should be cleaned.");
    }

    @Test
    void testBurstRequests() {
        FlowControlHandler.init(new InitMetaData("resource-burst", FlowControlConstant.LEAKY_BUCKET, 10, 1));

        for (int i = 0; i < 10; i++) {
            assertTrue(FlowControlHandler.canPass("resource-burst"), "Request should pass under the limit.");
        }

        for (int i = 0; i < 5; i++) {
            assertFalse(FlowControlHandler.canPass("resource-burst"), "Request should fail when limit exceeded.");
        }
    }

    @Test
    void testIntervalRequests() throws InterruptedException {
        FlowControlHandler.init(new InitMetaData("testIntervalRequests", FlowControlConstant.LEAKY_BUCKET, 5, 1));

        for (int i = 0; i < 5; i++) {
            assertTrue(FlowControlHandler.canPass("testIntervalRequests"));
            Thread.sleep(300); // Ensure requests are spaced within the interval
        }
    }

    @Test
    void testZeroThreshold() {
        // 测试抛出异常的情况
        Exception exception = assertThrows(FlowControlException.class, () -> FlowControlHandler.init(new InitMetaData("testZeroThreshold", FlowControlConstant.LEAKY_BUCKET, 0, 1)));
        assertEquals("count must be greater than 0", exception.getMessage());
        FlowControlHandler.canPass("testZeroThreshold");
    }
}
package com.wei.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// 压测执行类
class BenchmarkRunner {
    private final BenchmarkConfig config;
    private final BenchmarkTask task;

    public BenchmarkRunner(BenchmarkConfig config, BenchmarkTask task) {
        this.config = config;
        this.task = task;
    }

    ExecutorService executorService;

    public BenchmarkResult run() throws InterruptedException {

        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(config.getThreadCount());
        }

        List<Future<Long>> futures = new ArrayList<>();
        List<Long> responseTimes = new ArrayList<>();

        int totalRequests = config.getTotalRequests();
        CountDownLatch latch = new CountDownLatch(totalRequests);

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < totalRequests; i++) {
            futures.add(executorService.submit(() -> {
                long start = System.currentTimeMillis();
                try {
                    boolean success = task.execute();
                    if (success) {
                        return System.currentTimeMillis() - start;
                    } else {
                        throw new Exception("Task failed");
                    }
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();

        for (Future<Long> future : futures) {
            try {
                responseTimes.add(future.get());
                successCount++;
            } catch (Exception e) {
                failureCount++;
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        BenchmarkResult benchmarkResult = new BenchmarkResult(totalRequests, successCount, failureCount, totalTime, responseTimes);
        executorService.shutdown();

        return benchmarkResult;
    }
}

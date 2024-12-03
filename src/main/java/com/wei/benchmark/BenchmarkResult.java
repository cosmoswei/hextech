package com.wei.benchmark;

import lombok.Data;

import java.util.List;

// 压测结果类
@Data
class BenchmarkResult {
    private final int totalRequests;
    private final int successCount;
    private final int failureCount;
    private final long totalTime;
    private final List<Long> responseTimes;

    public BenchmarkResult(int totalRequests, int successCount, int failureCount, long totalTime, List<Long> responseTimes) {
        this.totalRequests = totalRequests;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.totalTime = totalTime;
        this.responseTimes = responseTimes;
    }

    public void printSummaryToConsole() {
        System.out.println("--- Benchmark Summary ---");
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Success Count: " + successCount);
        System.out.println("Failure Count: " + failureCount);
        System.out.println("Total Time (ms): " + totalTime);
        System.out.println("Average Response Time (ms): " + getAverageResponseTime());
        System.out.println("Max Response Time (ms): " + getMaxResponseTime());
        System.out.println("Min Response Time (ms): " + getMinResponseTime());
        System.out.println("QPS: " + getQPS());
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public List<Long> getResponseTimes() {
        return responseTimes;
    }

    public double getAverageResponseTime() {
        return responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    public long getMaxResponseTime() {
        return responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
    }

    public long getMinResponseTime() {
        return responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
    }

    public double getQPS() {
        return totalRequests / (totalTime / 1000.0);
    }
}

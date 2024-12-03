package com.wei.benchmark;

// 压测配置类
class BenchmarkConfig {
    private final int threadCount;
    private final int totalRequests;
    private final TaskType taskType;

    public BenchmarkConfig(int threadCount, int totalRequests, TaskType taskType) {
        this.threadCount = threadCount;
        this.totalRequests = totalRequests;
        this.taskType = taskType;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public TaskType getTaskType() {
        return taskType;
    }
}

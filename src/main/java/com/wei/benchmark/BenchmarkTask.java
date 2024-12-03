package com.wei.benchmark;

// 压测任务接口
@FunctionalInterface
public interface BenchmarkTask {
    boolean execute() throws Exception;
}
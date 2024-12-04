package com.wei.benchmark;

import lombok.Data;

// 分组结果类
@Data
class BenchmarkGroupResult {
    private final int requestCount;
    private final BenchmarkResult result;

    public BenchmarkGroupResult(int requestCount, BenchmarkResult result) {
        this.requestCount = requestCount;
        this.result = result;
    }
}

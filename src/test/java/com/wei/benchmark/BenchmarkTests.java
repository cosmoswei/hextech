package com.wei.benchmark;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BenchmarkTests {

    @Test
    public void benchmarkDemo() {
        List<Integer> requestCounts = Lists.newArrayList(100, 200, 500,
                1000, 2000, 5000,
                10000, 20000, 50000,
                100000, 200000, 500000);
        BenchmarkTask task = () -> {
            Thread.sleep(20);
            return false;
        };
        Benchmark.benchmark(requestCounts, task, 1000);
    }
}

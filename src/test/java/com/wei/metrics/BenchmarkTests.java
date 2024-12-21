package com.wei.metrics;

import com.google.common.collect.Lists;
import com.wei.benchmark.Benchmark;
import com.wei.benchmark.BenchmarkTask;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BenchmarkTests {

    /**
     * --- Benchmark Summary Table ---
     * Requests        Success         Failures        Total Time (ms)    Avg Time (ms)   Max Time (ms)   QPS
     * ----------------------------------------------------------------------------------------------------------
     * 100             100             0               444                154.83          303             225.23
     * 200             200             0               602                91.59           240             332.23
     * 500             500             0               2195               104.95          364             227.79
     * 1000            1000            0               12611              328.58          1583            79.30
     * 2000            2000            0               48754              1549.37         4895            41.02
     */
    @Test
    public void testMetrics() {
        PerformanceQueryUtils.init();
        List<Integer> requestCounts = Lists.newArrayList(100, 200, 500, 1000, 2000);
        BenchmarkTask task = () -> {
            PerformanceQueryUtils.getMetrics();
            return true;
        };
        Benchmark.benchmark(requestCounts, task, 1000);
    }

    /**
     * --- Benchmark Summary Table ---
     * Requests        Success         Failures        Total Time (ms)    Avg Time (ms)   Max Time (ms)   QPS
     * ----------------------------------------------------------------------------------------------------------
     * 100             100             0               514                281.46          482             194.55
     * 200             200             0               737                164.69          512             271.37
     * 500             500             0               1479               734.24          1327            338.07
     * 1000            1000            0               2790               851.09          1827            358.42
     * 2000            2000            0               5424               1563.52         3755            368.73
     * 5000            5000            0               14308              2675.40         9287            349.45
     * 10000           10000           0               28318              2619.20         13899           353.13
     * 20000           20000           0               56634              2717.89         11342           353.14
     * 50000           50000           0               143302             2846.43         14055           348.91
     */
    @Test
    public void testNetworkMetrics() {
        PerformanceQueryUtils.init();
        List<Integer> requestCounts = Lists.newArrayList(
                100, 200, 500,
                1000, 2000, 5000,
                10000, 20000, 50000
        );
        BenchmarkTask task = () -> {
            PerformanceQueryUtils.getNetworkMetrics();
            return true;
        };
        Benchmark.benchmark(requestCounts, task, 1000);
    }


    /**
     * --- Benchmark Summary Table ---
     * Requests        Success         Failures        Total Time (ms)    Avg Time (ms)   Max Time (ms)   QPS
     * ----------------------------------------------------------------------------------------------------------
     * 100             100             0               44                 3.54            10              2272.73
     * 200             200             0               118                4.57            19              1694.92
     * 500             500             0               1159               21.98           100             431.41
     * 1000            1000            0               8331               58.75           301             120.03
     * 2000            2000            0               47926              1507.37         4953            41.73
     * 5000            5000            0               112842             3891.09         11110           44.31
     * 10000           10000           0               233071             6803.57         15396           42.91
     * 20000           20000           0               480216             10106.87        22269           41.65
     * 50000           50000           0               1203113            15803.57        39975           41.56
     */
    @Test
    public void testThreadMetrics() {
        PerformanceQueryUtils.init();
        List<Integer> requestCounts = Lists.newArrayList(100, 200, 500,
                1000, 2000, 5000,
                10000, 20000, 50000
        );
        BenchmarkTask task = () -> {
            PerformanceQueryUtils.getThreadMetrics();
            return true;
        };
        Benchmark.benchmark(requestCounts, task, 1000);
    }

    /**
     * --- Benchmark Summary Table ---
     * Requests        Success         Failures        Total Time (ms)    Avg Time (ms)   Max Time (ms)   QPS
     * ----------------------------------------------------------------------------------------------------------
     * 100             100             0               23                 0.58            12              4347.83
     * 200             200             0               16                 0.16            1               12500.00
     * 500             500             0               30                 0.05            3               16666.67
     * 1000            1000            0               50                 0.02            1               20000.00
     * 2000            2000            0               55                 0.02            5               36363.64
     * 5000            5000            0               77                 0.01            1               64935.06
     * 10000           10000           0               117                0.02            29              85470.09
     * 20000           20000           0               170                0.01            2               117647.06
     * 50000           50000           0               228                0.01            13              219298.25
     * 100000          100000          0               400                0.02            18              250000.00
     * 200000          200000          0               585                0.01            23              341880.34
     * 500000          500000          0               1360               0.01            69              367647.06
     * 1000000         1000000         0               2930               0.01            140             341296.93
     * 2000000         2000000         0               6104               0.01            964             327654.00
     * 5000000         5000000         0               16317              0.01            3514            306428.88
     * 10000000        10000000        0               47486              0.03            6133            210588.38
     */
    @Test
    public void testGcMetrics() {
        PerformanceQueryUtils.init();
        List<Integer> requestCounts = Lists.newArrayList(100, 200, 500
                , 1000, 2000, 5000
                , 10000, 20000, 50000
                , 100000, 200000, 500000
                , 1000000, 2000000, 5000000
                , 10000000
        );
        BenchmarkTask task = () -> {
            PerformanceQueryUtils.getGcMetrics();
            return true;
        };
        Benchmark.benchmark(requestCounts, task, 1000);
    }

    /**
     * --- Benchmark Summary Table ---
     * Requests        Success         Failures        Total Time (ms)    Avg Time (ms)   Max Time (ms)   QPS
     * ----------------------------------------------------------------------------------------------------------
     * 100             100             0               168                17.94           88              595.24
     * 200             200             0               431                18.72           116             464.04
     * 500             500             0               516                17.80           174             968.99
     * 1000            1000            0               273                1.75            13              3663.00
     * 2000            2000            0               744                43.38           466             2688.17
     * 5000            5000            0               1193               121.44          905             4191.11
     * 10000           10000           0               2357               177.24          1967            4242.68
     * 20000           19999           1               5695               255.55          1768            3511.85
     * 50000           49999           1               14477              276.92          2253            3453.75
     * 100000          100000          0               29369              288.40          3813            3404.95
     * 200000          199999          1               58078              286.80          3015            3443.64
     * 500000          499997          3               144785             288.50          3122            3453.40
     */
    @Test
    public void testSystemMetrics() {
        PerformanceQueryUtils.init();
        List<Integer> requestCounts = Lists.newArrayList(100, 200, 500
                , 1000, 2000, 5000
                , 10000, 20000, 50000
                , 100000, 200000, 500000
        );
        BenchmarkTask task = () -> {
            PerformanceQueryUtils.getSystemMetrics();
            return true;
        };
        Benchmark.benchmark(requestCounts, task, 1000);
    }
}

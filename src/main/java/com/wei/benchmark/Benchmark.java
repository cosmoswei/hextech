package com.wei.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {

    public static void benchmark(List<Integer> loopCounts, BenchmarkTask task, int threadCount) {
        List<BenchmarkGroupResult> groupResults = new ArrayList<>();
        for (int requestCount : loopCounts) {
            try {
                Thread.sleep(20);
                System.out.println("Running benchmark for " + requestCount + " requests...");
                BenchmarkConfig config = new BenchmarkConfig(threadCount, requestCount, TaskType.LOCAL);
                task.execute();
                // 执行压测
                BenchmarkRunner runner = new BenchmarkRunner(config, task);
                BenchmarkResult result = runner.run();

                // 输出压测结果
                result.printSummaryToConsole();

                // 收集结果分组
                groupResults.add(new BenchmarkGroupResult(requestCount, result));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 导出所有分组结果到一个 JSON 文件
        exportGroupResultsToJson(groupResults, "benchmark/benchmark_results.json");
        // 按表格格式汇总输出到控制台
        printSummaryTable(groupResults);
    }

    private static void exportGroupResultsToJson(List<BenchmarkGroupResult> groupResults, String filePath) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Failed to create directory " + parentDir.getPath());
                }
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(filePath), groupResults);
            System.out.println("Results exported to " + filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printSummaryTable(List<BenchmarkGroupResult> groupResults) {
        System.out.println("\n--- Benchmark Summary Table ---");
        System.out.printf("%-15s %-15s %-15s %-18s %-15s %-15s %-15s%n", "Requests", "Success", "Failures", "Total Time (ms)", "Avg Time (ms)", "Max Time (ms)", "QPS");
        System.out.println("----------------------------------------------------------------------------------------------------------");

        for (BenchmarkGroupResult groupResult : groupResults) {
            BenchmarkResult result = groupResult.getResult();
            System.out.printf("%-15d %-15d %-15d %-18d %-15.2f %-15d %-15.2f%n",
                    groupResult.getRequestCount(),
                    result.getSuccessCount(),
                    result.getFailureCount(),
                    result.getTotalTime(),
                    result.getAverageResponseTime(),
                    result.getMaxResponseTime(),
                    result.getQPS());
        }
    }
}



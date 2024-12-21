package com.wei.metrics;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import com.wei.metrics.obj.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.FileSystem;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PerformanceQueryUtils {

    private static final Logger log = LoggerFactory.getLogger(PerformanceQueryUtils.class);
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final SystemInfo SI = new SystemInfo();
    private static final HardwareAbstractionLayer HAL = SI.getHardware();
    private static final OperatingSystem OS = SI.getOperatingSystem();
    private static final MeterRegistry REGISTRY = MetricRegistryProvider.getRegistry();
    private static final int MINUTE = 1000 * 60;
    private static final int MB = 1024 * 1024;

    /**
     * GC 最后更新时间
     */
    private static long gcLastTime = 0;
    /**
     * GC 次数
     */
    private static int gcCount = 0;
    /**
     * GC 计时 Map <time,avgLatency>
     */
    static Map<Long, Long> gcTimeMap = new ConcurrentHashMap<>();
    /**
     * CPU 使用率计时 Map <time,CpuUsage>
     */
    static Map<Long, CpuUsage> cpuTimeMap = new ConcurrentHashMap<>();
    /**
     * IOPS 计时 Map <time,iops>
     */
    static Map<Long, Long> iopsMap = new HashMap<>();

    public static void init() {
        new ClassLoaderMetrics().bindTo(REGISTRY);
        new JvmMemoryMetrics().bindTo(REGISTRY);
        new JvmGcMetrics().bindTo(REGISTRY);
        new JvmThreadMetrics().bindTo(REGISTRY);
        EXECUTOR_SERVICE.submit(() -> {
            for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                NotificationEmitter emitter = (NotificationEmitter) gcBean;
                NotificationListener notificationListener = (notification, handback) -> {
                    if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                        GarbageCollectionNotificationInfo from = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                        GcInfo gcInfo = from.getGcInfo();
                        long duration = gcInfo.getDuration();
                        log.debug("{} GC duration: {}", from.getGcName(), duration);
                        gcLastTime = System.currentTimeMillis();
                        gcTimeMap.put(gcLastTime, duration);
                    }
                };
                emitter.addNotificationListener(notificationListener, null, null);
            }
        });
        new Thread(() -> {
            while (true) {
                CpuUsage cpuUsage = getCpuUsage(HAL.getProcessor());
                cpuTimeMap.put(System.currentTimeMillis(), cpuUsage);
                long iops = getIops();
                iopsMap.put(System.currentTimeMillis(), iops);
                log.debug("CPU usage: {}", cpuUsage);
                log.debug("IOPS: {}", iops);
                Util.sleep(2000);
            }
        }).start();

    }

    public static void main(String[] args) {
        PerformanceQueryUtils performanceQueryUtils = new PerformanceQueryUtils();
        for (int i = 0; i < 100; i++) {
            byte[] bytes = new byte[MB * 20];
            Util.sleep(10);
            System.gc();
        }
        Util.sleep(4000);
        long start = System.currentTimeMillis();
        System.out.println("performanceQueryUtils.getGcMetrics() = " + performanceQueryUtils.getMetrics());
        long end = System.currentTimeMillis();
        System.out.println("(end-start) = " + (end - start));
        EXECUTOR_SERVICE.shutdownNow();
    }

    public static Metrics getMetrics() {
        Metrics metrics = new Metrics();
        metrics.setSystemMetrics(getSystemMetrics());
        metrics.setGcMetrics(getGcMetrics());
        metrics.setNetworkMetrics(getNetworkMetrics());
        metrics.setThreadMetrics(getThreadMetrics());
        return metrics;
    }

    public static SystemMetrics getSystemMetrics() {
        SystemMetrics systemMetrics = new SystemMetrics();
        CentralProcessor processor = HAL.getProcessor();

        // CPU
        int cpuCount = processor.getLogicalProcessorCount();
        systemMetrics.setCpuCount(cpuCount);
        double[] loadAverage = processor.getSystemLoadAverage(3);
        systemMetrics.setCpuLoad1m(NumberTool.round(loadAverage[0] < 0 ? 0 : loadAverage[0], 2));
        systemMetrics.setCpuLoad5m(NumberTool.round(loadAverage[1] < 0 ? 0 : loadAverage[1], 2));
        systemMetrics.setCpuLoad15m(NumberTool.round(loadAverage[2] < 0 ? 0 : loadAverage[2], 2));
        systemMetrics.setContextSwitches(processor.getContextSwitches());
        systemMetrics.setInterrupts(processor.getInterrupts());
        CpuUsage cpuUsage = getCpuUsage();
        systemMetrics.setCpuUsage(cpuUsage);

        // 内存
        GlobalMemory memory = HAL.getMemory();
        systemMetrics.setMemoryTotal(NumberTool.round((double) memory.getTotal() / MB, 2));
        systemMetrics.setMemoryAvailable(NumberTool.round((double) memory.getAvailable() / MB, 2));
        long swapUsed = memory.getVirtualMemory().getSwapUsed();
        long swapTotal = memory.getVirtualMemory().getSwapTotal();
        systemMetrics.setSwapUsed(NumberTool.round((double) swapUsed / MB, 2));
        systemMetrics.setSwapTotal(NumberTool.round((double) swapTotal / MB, 2));

        // 线程
        systemMetrics.setProcessCount(OS.getProcessCount());
        systemMetrics.setThreadCount(OS.getThreadCount());
        FileSystem fileSystem = OS.getFileSystem();

        // 文件描述符
        systemMetrics.setOpenFdCount(fileSystem.getOpenFileDescriptors());
        systemMetrics.setIops(getIops1s());
        return systemMetrics;
    }

    private static CpuUsage getCpuUsage() {
        long now = System.currentTimeMillis();
        // 1s 之前的不要
        cpuTimeMap.entrySet().removeIf((k) -> k.getKey() < now - 1000);
        return cpuTimeMap.entrySet().stream()
                .max(Map.Entry.comparingByKey())  // 比较 key
                .map(Map.Entry::getValue).orElse(null);
    }

    private static Long getIops1s() {
        long now = System.currentTimeMillis();
        // 1s 之前的不要
        iopsMap.entrySet().removeIf((k) -> k.getKey() < now - 1000);
        return iopsMap.entrySet().stream()
                .max(Map.Entry.comparingByKey())  // 比较 key
                .map(Map.Entry::getValue).orElse(0L);
    }


    private static CpuUsage getCpuUsage(CentralProcessor processor) {
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        // Wait a second
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        CpuUsage cpuUsage = new CpuUsage();
        cpuUsage.setCpuUsrUsage(NumberTool.div(100 * user, totalCpu, 2));
        cpuUsage.setCpuSysUsage(NumberTool.div(100 * sys, totalCpu, 2));
        cpuUsage.setCpuIdle(NumberTool.div(100 * idle, totalCpu, 2));
        return cpuUsage;
    }

    private static long getIops() {
        Map<String, Map<String, Long>> IOPSMap = new ConcurrentHashMap<>();
        for (HWDiskStore hwDiskStore : HAL.getDiskStores()) {
            String name = hwDiskStore.getName();
            long readsBefore = hwDiskStore.getReads();
            long writesBefore = hwDiskStore.getWrites();
            Map<String, Long> beforeMap = new HashMap<>();
            beforeMap.put("readsBefore", readsBefore);
            beforeMap.put("writesBefore", writesBefore);
            IOPSMap.put(name, beforeMap);
        }

        // 记录一秒的读写差
        Util.sleep(1000);

        for (HWDiskStore hwDiskStore : HAL.getDiskStores()) {
            String name = hwDiskStore.getName();
            Map<String, Long> afterMap = IOPSMap.get(name);
            long readsAfter = hwDiskStore.getReads();
            long writesAfter = hwDiskStore.getWrites();
            afterMap.put("readsAfter", readsAfter);
            afterMap.put("writesAfter", writesAfter);
        }
        long iops = 0;
        for (Map.Entry<String, Map<String, Long>> stringMapEntry : IOPSMap.entrySet()) {
            Map<String, Long> value = stringMapEntry.getValue();
            Long readsBefore = value.get("readsBefore");
            Long writesBefore = value.get("writesBefore");
            Long readsAfter = value.get("readsAfter");
            Long writesAfter = value.get("writesAfter");
            iops += (readsAfter - readsBefore) + (writesAfter - writesBefore);
        }
        return iops;
    }

    public static NetworkMetrics getNetworkMetrics() {
        NetworkMetrics networkMetrics = new NetworkMetrics();
        networkMetrics.setLatency(0.0D);
        networkMetrics.setBandwidth(0.0D);
        for (NetworkIF net : HAL.getNetworkIFs()) {
            // 过滤虚拟网卡
            if (null == net.getIPv4addr()
                    || net.getIPv4addr().length == 0
                    || null == net.getIPv6addr()
                    || net.getIPv6addr().length == 0) {
                continue;
            }
            networkMetrics.setMtu(net.getMTU());
            networkMetrics.setSpeed(net.getSpeed());
            networkMetrics.setMacaddr(net.getMacaddr());
            networkMetrics.setIPv4addr(net.getIPv4addr());
            networkMetrics.setIPv6addr(net.getIPv6addr());
            boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0
                    || net.getPacketsSent() > 0;
            networkMetrics.setHasData(hasData);
            networkMetrics.setBytesRecv(net.getBytesRecv());
            networkMetrics.setBytesSent(net.getBytesSent());
            networkMetrics.setPacketsRecv(net.getPacketsRecv());
            networkMetrics.setPacketsSent(net.getPacketsSent());
            networkMetrics.setInErrors(net.getInErrors());
            networkMetrics.setOutErrors(net.getOutErrors());
        }
        return networkMetrics;
    }

    public static GcMetrics getGcMetrics() {
        GcMetrics gcMetrics = new GcMetrics();
        gcMetrics.setGcPauseTime1m(NumberTool.round(getGcPauseTime1m(), 2));
        gcMetrics.setGcThroughput1m(NumberTool.round(getGcThroughput1m(), 2));
        gcMetrics.setGcCount1m(gcCount);
        gcMetrics.setGcMemoryPoolUsage(0.0D);
        gcMetrics.setGcHeapAllocationRate(NumberTool.round(getAllocationRate(REGISTRY), 2));
        gcMetrics.setGcHeapPromotedRate(NumberTool.round(getPromotedRate(REGISTRY), 2));
        return gcMetrics;
    }

    public static ThreadMetrics getThreadMetrics() {
        ThreadMetrics threadMetrics = new ThreadMetrics();
        double liveThreads = REGISTRY.get("jvm.threads.live").gauge().value();
        threadMetrics.setLiveThreads(liveThreads);
        double peakThreads = REGISTRY.get("jvm.threads.peak").gauge().value();
        threadMetrics.setPeakThreads(peakThreads);
        double blockedThreads = REGISTRY.get("jvm.threads.states").tag("state", "blocked").gauge().value();
        threadMetrics.setBlockedThreads(blockedThreads);
        double runnableThreads = REGISTRY.get("jvm.threads.states").tag("state", "runnable").gauge().value();
        threadMetrics.setRunnableThreads(runnableThreads);
        double newThreads = REGISTRY.get("jvm.threads.states").tag("state", "new").gauge().value();
        threadMetrics.setNewThreads(newThreads);
        double timedWaitingThreads = REGISTRY.get("jvm.threads.states").tag("state", "timed-waiting").gauge().value();
        threadMetrics.setTimedWaitingThreads(timedWaitingThreads);
        double terminatedThreads = REGISTRY.get("jvm.threads.states").tag("state", "terminated").gauge().value();
        threadMetrics.setTerminatedThreads(terminatedThreads);
        double waitingThreads = REGISTRY.get("jvm.threads.states").tag("state", "waiting").gauge().value();
        threadMetrics.setWaitingThreads(waitingThreads);
        return threadMetrics;
    }

    private static double getGcPauseTime1m() {
        long now = System.currentTimeMillis();
        gcTimeMap.entrySet().removeIf((k) -> k.getKey() < now - MINUTE);
        // 去掉一分钟以前的
        gcCount = gcTimeMap.size();
        OptionalDouble average = gcTimeMap.values().stream().mapToLong(l -> l).average();
        return average.orElse(0.0D);
    }

    private static double getGcThroughput1m() {
        long now = System.currentTimeMillis();
        // 去掉一分钟以前的
        gcTimeMap.entrySet().removeIf((k) -> k.getKey() < now - MINUTE);
        gcCount = gcTimeMap.size();
        long sum = gcTimeMap.values().stream().mapToLong(l -> l).sum();
        return NumberTool.div(60 * 1000 - sum, 60 * 1000, 4) * 100;
    }


    private static double getAllocationRate(MeterRegistry registry) {
        double memoryAllocated = registry.get("jvm.gc.memory.allocated").counter().count();
        return memoryAllocated / 1024 / 1024;
    }

    private static double getPromotedRate(MeterRegistry registry) {
        double memoryPromoted = registry.get("jvm.gc.memory.promoted").counter().count();
        return memoryPromoted / 1024 / 1024;
    }
}

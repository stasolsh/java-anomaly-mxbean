package com.example.anomaly.detectgcthrash;

import java.lang.management.*;
import java.util.List;

public final class GcAnomalyProbe {
    private final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();

    private long lastUptimeMs = runtime.getUptime();
    private long lastGcTimeMs = totalGcTimeMs();
    private long lastGcCount = totalGcCount();

    public record GcSnapshot(
            long intervalMs,
            long gcTimeMs,
            long gcCount,
            long heapUsed,
            long heapMax,
            double gcCpuRatio,
            double heapUsedRatio
    ) {
    }

    public GcSnapshot sample() {
        long nowUptime = runtime.getUptime();
        long nowGcTime = totalGcTimeMs();
        long nowGcCount = totalGcCount();

        long interval = Math.max(1, nowUptime - lastUptimeMs);
        long gcTimeDelta = Math.max(0, nowGcTime - lastGcTimeMs);
        long gcCountDelta = Math.max(0, nowGcCount - lastGcCount);

        MemoryUsage heap = memory.getHeapMemoryUsage();
        long used = heap.getUsed();
        long max = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();

        double gcRatio = gcTimeDelta / (double) interval;      // e.g. 0.25 = 25% time in GC
        double heapRatio = used / (double) max;                // e.g. 0.90 = 90% heap used

        lastUptimeMs = nowUptime;
        lastGcTimeMs = nowGcTime;
        lastGcCount = nowGcCount;

        return new GcSnapshot(interval, gcTimeDelta, gcCountDelta, used, max, gcRatio, heapRatio);
    }

    public boolean isGcThrashing(GcSnapshot s) {
        return s.gcCpuRatio() > 0.20 && s.gcCount() >= 5;      // tune per app
    }

    public boolean isHeapHigh(GcSnapshot s) {
        return s.heapUsedRatio() > 0.85;
    }

    private long totalGcTimeMs() {
        long sum = 0;
        for (GarbageCollectorMXBean gc : gcs) {
            long t = gc.getCollectionTime();
            if (t > 0) sum += t;
        }
        return sum;
    }

    private long totalGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean gc : gcs) {
            long c = gc.getCollectionCount();
            if (c > 0) sum += c;
        }
        return sum;
    }
}

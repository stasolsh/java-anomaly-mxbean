package com.example.anomaly.detectgcthrash;

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

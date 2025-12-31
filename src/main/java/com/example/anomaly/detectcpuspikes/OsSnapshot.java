package com.example.anomaly.detectcpuspikes;

public record OsSnapshot(double processCpuLoad, double systemCpuLoad, long openFds, long maxFds) {
}


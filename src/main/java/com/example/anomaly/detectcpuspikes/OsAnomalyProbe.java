package com.example.anomaly.detectcpuspikes;

import com.sun.management.UnixOperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public final class OsAnomalyProbe {

    public record OsSnapshot(double processCpuLoad, double systemCpuLoad, long openFds, long maxFds) {
    }

    // Get it once; it’s effectively a singleton bean.
    private static final OperatingSystemMXBean BASE = ManagementFactory.getOperatingSystemMXBean();

    // Cast once (if supported)
    private static final com.sun.management.OperatingSystemMXBean OS = (BASE instanceof com.sun.management.OperatingSystemMXBean os) ? os : null;

    private static final UnixOperatingSystemMXBean UNIX =
            (BASE instanceof UnixOperatingSystemMXBean u) ? u : null;

    /**
     * One sample. CPU load may be -1.0 if not available yet.
     */
    public OsSnapshot sample() {
        if (OS == null) {
            return new OsSnapshot(-1.0, -1.0, -1L, -1L);
        }

        double processLoad = OS.getProcessCpuLoad(); // 0..1 or -1
        double systemLoad = OS.getSystemCpuLoad();  // 0..1 or -1

        long open = -1, max = -1;
        if (UNIX != null) {
            open = UNIX.getOpenFileDescriptorCount();
            max = UNIX.getMaxFileDescriptorCount();
        }

        return new OsSnapshot(processLoad, systemLoad, open, max);
    }

    public boolean cpuSpike(OsSnapshot s) {
        return s.processCpuLoad() >= 0.0 && s.processCpuLoad() > 0.90;
    }

    public boolean fdLeakSuspected(OsSnapshot s) {
        return s.openFds() >= 0 && s.maxFds() > 0 && (s.openFds() / (double) s.maxFds()) > 0.80;
    }
}

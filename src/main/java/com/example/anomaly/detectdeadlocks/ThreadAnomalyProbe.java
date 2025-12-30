package com.example.anomaly.detectdeadlocks;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

public final class ThreadAnomalyProbe {
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();

    public ThreadAnomalyProbe() {
        if (threads.isThreadContentionMonitoringSupported()) {
            threads.setThreadContentionMonitoringEnabled(true);
        }
        if (threads.isThreadCpuTimeSupported()) {
            threads.setThreadCpuTimeEnabled(true);
        }
    }

    public long[] findDeadlockedThreads() {
        // Works for both monitor deadlocks and ownable synchronizers
        return threads.findDeadlockedThreads();
    }

    public void printTopBlockedThreads(long topN) {
        long[] ids = threads.getAllThreadIds();
        ThreadInfo[] infos = threads.getThreadInfo(ids, Integer.MAX_VALUE);

        List<ThreadInfo> list = new ArrayList<>();
        for (ThreadInfo ti : infos) if (ti != null) list.add(ti);

        list.sort((a, b) -> Long.compare(b.getBlockedTime(), a.getBlockedTime()));

        for (int i = 0; i < Math.min(topN, list.size()); i++) {
            ThreadInfo t = list.get(i);
            System.out.printf("BLOCKED: %s blockedTime=%dms blockedCount=%d lock=%s owner=%s%n",
                    t.getThreadName(), t.getBlockedTime(), t.getBlockedCount(),
                    t.getLockName(), t.getLockOwnerName());
        }
    }
}

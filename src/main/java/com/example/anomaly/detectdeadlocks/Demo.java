package com.example.anomaly.detectdeadlocks;

import java.util.Arrays;

public class Demo {
    public static void main(String[] args) {
        var probe = new ThreadAnomalyProbe();
        while (true) {
            long[] deadlocked = probe.findDeadlockedThreads();
            if (deadlocked != null && deadlocked.length > 0) {
                System.out.println("ANOMALY: Deadlock detected! threads=" + deadlocked.length);
                Arrays.stream(deadlocked).forEach(probe::printTopBlockedThreads);
                // precisely trace information about deadlocks
            }
        }
    }
}

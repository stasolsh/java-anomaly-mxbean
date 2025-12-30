package com.example.anomaly.detectdeadlocks;

public class Demo {
    public static void main(String[] args) {
        var probe = new ThreadAnomalyProbe();
        while (true) {
            long[] deadlocked = probe.findDeadlockedThreads();
            if (deadlocked != null && deadlocked.length > 0) {
                System.out.println("ANOMALY: Deadlock detected! threads=" + deadlocked.length);
                // precisely trace information about deadlocks
            }
        }
    }
}

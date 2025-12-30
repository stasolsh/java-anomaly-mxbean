package com.example.anomaly.detectgcthrash;

/**
 * If GC time in the last minute is high (e.g., > 20% of time)
 * OR heap usage stays > 85%, raise an anomaly.
 */
public class Demo {
    public static void main(String[] args) throws Exception {
        var probe = new GcAnomalyProbe();
        while (true) {
            var s = probe.sample();
            if (probe.isGcThrashing(s) || probe.isHeapHigh(s)) {
                System.out.printf("ANOMALY: gcRatio=%.2f heapRatio=%.2f gcCount=%d%n",
                        s.gcCpuRatio(), s.heapUsedRatio(), s.gcCount());
            }
            Thread.sleep(10_000);
        }
    }
}

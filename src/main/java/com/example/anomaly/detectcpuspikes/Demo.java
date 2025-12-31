package com.example.anomaly.detectcpuspikes;

public class Demo {
    public static void main(String[] args) throws Exception {
        OsAnomalyProbe probe = new OsAnomalyProbe();

        // Warm up: first call often returns -1.0
        probe.sample();
        Thread.sleep(1000);

        while (true) {
            var s = probe.sample();

            System.out.printf("processCpu=%.3f systemCpu=%.3f openFD=%d maxFD=%d%n",
                    s.processCpuLoad(), s.systemCpuLoad(), s.openFds(), s.maxFds());

            if (probe.cpuSpike(s)) {
                System.out.println("ANOMALY: CPU spike (process)!");
            }
            if (probe.fdLeakSuspected(s)) {
                System.out.println("ANOMALY: FD usage high!");
            }

            Thread.sleep(5000);
        }
    }
}


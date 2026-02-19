package com.example.anomaly.anomalymonitor;

import com.example.anomaly.detectcpuspikes.OsAnomalyProbe;
import com.example.anomaly.detectdeadlocks.ThreadAnomalyProbe;
import com.example.anomaly.detectgcthrash.GcAnomalyProbe;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class AnomalyMonitor extends NotificationBroadcasterSupport implements AnomalyMXBean {
    private final GcAnomalyProbe gcProbe = new GcAnomalyProbe();
    private final ThreadAnomalyProbe threadProbe = new ThreadAnomalyProbe();
    private final OsAnomalyProbe osProbe = new OsAnomalyProbe();

    private volatile double heapHighThreshold = 0.85;
    private volatile double gcThrashRatioThreshold = 0.20;

    private volatile boolean gcThrashing;
    private volatile boolean heapHigh;
    private volatile boolean cpuSpiking;
    private volatile boolean deadlocked;

    private long seq = 1;

    public void tick() {
        var gc = gcProbe.sample();
        var os = osProbe.sample();
        var dl = threadProbe.findDeadlockedThreads();

        setFlag("GC_THRASH", (gc.gcCpuRatio() > gcThrashRatioThreshold && gc.gcCount() >= 5),
                v -> gcThrashing = v);
        setFlag("HEAP_HIGH", (gc.heapUsedRatio() > heapHighThreshold),
                v -> heapHigh = v);
        setFlag("CPU_SPIKE", os.processCpuLoad() >= 0 && os.processCpuLoad() > 0.90,
                v -> cpuSpiking = v);
        setFlag("DEADLOCK", (dl != null && dl.length > 0),
                v -> deadlocked = v);
    }

    private void setFlag(String type, boolean newValue, Consumer<Boolean> setter) {
        boolean oldValue =
                switch (type) {
                    case "GC_THRASH" -> gcThrashing;
                    case "HEAP_HIGH" -> heapHigh;
                    case "CPU_SPIKE" -> cpuSpiking;
                    case "DEADLOCK" -> deadlocked;
                    default -> false;
                };

        if (oldValue != newValue) {
            setter.accept(newValue);
            sendNotification(new Notification(
                    "com.example.anomaly." + type,
                    this,
                    seq++,
                    System.currentTimeMillis(),
                    type + " changed to " + newValue
            ));
        }
    }

    @Override
    public boolean isGcThrashing() {
        return gcThrashing;
    }

    @Override
    public boolean isHeapHigh() {
        return heapHigh;
    }

    @Override
    public boolean isCpuSpiking() {
        return cpuSpiking;
    }

    @Override
    public boolean isDeadlocked() {
        return deadlocked;
    }

    @Override
    public List<String> getActiveAnomalies() {
        List<String> out = new ArrayList<>();
        if (gcThrashing) out.add("GC thrashing");
        if (heapHigh) out.add("Heap high");
        if (cpuSpiking) out.add("CPU spike");
        if (deadlocked) out.add("Deadlock detected");
        return out;
    }

    @Override
    public double getHeapHighThreshold() {
        return heapHighThreshold;
    }

    @Override
    public void setHeapHighThreshold(double v) {
        if (v <= 0 || v >= 1) throw new IllegalArgumentException("0 < v < 1");
        heapHighThreshold = v;
    }

    @Override
    public double getGcThrashRatioThreshold() {
        return gcThrashRatioThreshold;
    }

    @Override
    public void setGcThrashRatioThreshold(double v) {
        if (v <= 0 || v > 1) throw new IllegalArgumentException("0 < v < 1");
        gcThrashRatioThreshold = v;
    }

    public static void main(String[] args) {
        AnomalyMonitor anomalyMonitor = new AnomalyMonitor();
        anomalyMonitor.setGcThrashRatioThreshold(0.5);
        anomalyMonitor.setGcThrashRatioThreshold(1.0);
        anomalyMonitor.setHeapHighThreshold(0.5);
    }
}

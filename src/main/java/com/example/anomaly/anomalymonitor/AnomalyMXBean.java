package com.example.anomaly.anomalymonitor;

import javax.management.MXBean;
import java.util.List;

@MXBean
public interface AnomalyMXBean {
    boolean isGcThrashing();

    boolean isHeapHigh();

    boolean isCpuSpiking();

    boolean isDeadlocked();

    List<String> getActiveAnomalies();

    double getHeapHighThreshold();

    void setHeapHighThreshold(double v);

    double getGcThrashRatioThreshold();

    void setGcThrashRatioThreshold(double v);
}

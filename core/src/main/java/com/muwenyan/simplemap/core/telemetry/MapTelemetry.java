package com.muwenyan.simplemap.core.telemetry;

import java.util.EnumMap;
import java.util.Map;

public final class MapTelemetry {
    private final Map<MapMetric, Long> values = new EnumMap<>(MapMetric.class);

    public synchronized void add(MapMetric metric, long delta) {
        if (metric == null || delta < 0) throw new IllegalArgumentException("metric/delta");
        values.merge(metric, delta, Math::addExact);
    }

    public synchronized long value(MapMetric metric) {
        if (metric == null) throw new NullPointerException("metric");
        return values.getOrDefault(metric, 0L);
    }

    public synchronized Map<MapMetric, Long> snapshot() {
        return Map.copyOf(values);
    }
}

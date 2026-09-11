package com.muwenyan.simplemap.core.telemetry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapTelemetryTest {
    @Test
    void countersAreMonotonicAndSnapshotIsImmutable() {
        MapTelemetry telemetry = new MapTelemetry();
        telemetry.add(MapMetric.CHUNKS_REQUESTED, 2);
        telemetry.add(MapMetric.CHUNKS_REQUESTED, 3);
        assertEquals(5, telemetry.value(MapMetric.CHUNKS_REQUESTED));
        assertEquals(1, telemetry.snapshot().size());
    }
}

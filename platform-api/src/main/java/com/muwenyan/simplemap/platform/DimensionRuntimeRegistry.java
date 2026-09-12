package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DimensionRuntimeRegistry {
    private final Map<DimensionId, MapRegion> regions = new LinkedHashMap<>();
    private DimensionId active;

    public synchronized void put(MapRegion region) {
        Objects.requireNonNull(region, "region");
        regions.put(region.dimension(), region);
        active = region.dimension();
    }

    public synchronized void activate(DimensionId dimension) {
        Objects.requireNonNull(dimension, "dimension");
        if (!regions.containsKey(dimension)) {
            throw new IllegalArgumentException("dimension is not loaded: " + dimension.value());
        }
        active = dimension;
    }

    public synchronized Optional<MapRegion> activeRegion() {
        return Optional.ofNullable(active == null ? null : regions.get(active));
    }

    public synchronized Optional<MapRegion> region(DimensionId dimension) {
        return Optional.ofNullable(regions.get(Objects.requireNonNull(dimension, "dimension")));
    }

    public synchronized int size() {
        return regions.size();
    }
    public synchronized void remove(DimensionId dimension) {
        regions.remove(Objects.requireNonNull(dimension, "dimension"));
        if (dimension.equals(active)) active = regions.keySet().stream().findFirst().orElse(null);
    }
}

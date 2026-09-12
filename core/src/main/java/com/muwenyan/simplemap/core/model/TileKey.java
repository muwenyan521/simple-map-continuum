package com.muwenyan.simplemap.core.model;

import java.util.Objects;

public record TileKey(DimensionId dimension, RegionPos region, int lod, int x, int z) {
    public TileKey {
        dimension = Objects.requireNonNull(dimension, "dimension");
        region = Objects.requireNonNull(region, "region");
        if (lod < 0 || lod > 31) throw new IllegalArgumentException("lod out of range");
        if (x < 0 || z < 0) throw new IllegalArgumentException("tile coordinates must be non-negative");
    }

    public TileKey parent() {
        if (lod == 31) return this;
        return new TileKey(dimension, region, lod + 1, x / 2, z / 2);
    }
}

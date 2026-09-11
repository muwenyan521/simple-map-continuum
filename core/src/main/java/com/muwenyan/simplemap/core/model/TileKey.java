package com.muwenyan.simplemap.core.model;

import java.util.Objects;

public record TileKey(DimensionId dimension, RegionPos region, int lod, int x, int z) {
    public TileKey {
        dimension = Objects.requireNonNull(dimension, "dimension");
        region = Objects.requireNonNull(region, "region");
        if (lod < 0 || lod > 31) throw new IllegalArgumentException("lod out of range");
    }
}

package com.muwenyan.simplemap.core.streaming;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.Objects;

public record ChunkDemand(DimensionId dimension, ChunkPos center, int radius, int maxChunks) {
    public ChunkDemand {
        dimension = Objects.requireNonNull(dimension, "dimension");
        center = Objects.requireNonNull(center, "center");
        if (radius < 0) throw new IllegalArgumentException("radius must be non-negative");
        if (maxChunks < 1) throw new IllegalArgumentException("maxChunks must be positive");
        long side = (long) radius * 2 + 1;
        long radiusAsLong = radius;
        if ((long) center.x() - radiusAsLong < Integer.MIN_VALUE
                || (long) center.x() + radiusAsLong > Integer.MAX_VALUE
                || (long) center.z() - radiusAsLong < Integer.MIN_VALUE
                || (long) center.z() + radiusAsLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("demand exceeds chunk coordinate range");
        }
        if (side <= Integer.MAX_VALUE && (long) maxChunks > side * side) {
            throw new IllegalArgumentException("maxChunks exceeds demand area");
        }
    }
}

package com.muwenyan.simplemap.core.model;

import java.util.Objects;

public record ChunkSnapshot(DimensionId dimension, ChunkPos position, long revision, byte[] payload) {
    public ChunkSnapshot {
        dimension = Objects.requireNonNull(dimension, "dimension");
        position = Objects.requireNonNull(position, "position");
        if (revision < 0) throw new IllegalArgumentException("revision must be non-negative");
        payload = Objects.requireNonNull(payload, "payload").clone();
    }
    @Override public byte[] payload() { return payload.clone(); }
}

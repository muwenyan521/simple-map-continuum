package com.muwenyan.simplemap.core.streaming;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.Objects;

public record ChunkEvent(DimensionId dimension, ChunkPos position, ChunkEventKind kind, long revision) {
    public ChunkEvent {
        dimension = Objects.requireNonNull(dimension, "dimension");
        position = Objects.requireNonNull(position, "position");
        kind = Objects.requireNonNull(kind, "kind");
        if (revision < 0) throw new IllegalArgumentException("revision");
    }
}

package com.muwenyan.simplemap.core.streaming;

import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import java.util.Objects;
import java.util.Optional;

public record ChunkMutation(MutationKind kind, long storeRevision,
                            ChunkSnapshot current, Optional<ChunkSnapshot> previous) {
    public ChunkMutation {
        kind = Objects.requireNonNull(kind, "kind");
        if (storeRevision < 0) throw new IllegalArgumentException("storeRevision");
        current = Objects.requireNonNull(current, "current");
        previous = Objects.requireNonNull(previous, "previous");
    }
}

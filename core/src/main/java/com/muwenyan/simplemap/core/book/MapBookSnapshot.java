package com.muwenyan.simplemap.core.book;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record MapBookSnapshot(
        UUID id,
        UUID owner,
        MapBookStatus status,
        long revision,
        List<MapBookRegion> regions) {
    public MapBookSnapshot {
        id = Objects.requireNonNull(id, "id");
        owner = Objects.requireNonNull(owner, "owner");
        status = Objects.requireNonNull(status, "status");
        if (revision < 0) {
            throw new IllegalArgumentException("revision must be non-negative");
        }
        regions = List.copyOf(Objects.requireNonNull(regions, "regions"));
    }
}

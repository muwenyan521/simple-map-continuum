package com.muwenyan.simplemap.core.book;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Map;

public record MapBookSnapshot(
        UUID id,
        UUID owner,
        MapBookStatus status,
        long revision,
        List<MapBookRegion> regions,
        Map<UUID, BookPermission> permissions) {
    public MapBookSnapshot {
        id = Objects.requireNonNull(id, "id");
        owner = Objects.requireNonNull(owner, "owner");
        status = Objects.requireNonNull(status, "status");
        if (revision < 0) {
            throw new IllegalArgumentException("revision must be non-negative");
        }
        regions = List.copyOf(Objects.requireNonNull(regions, "regions"));
        permissions = Map.copyOf(Objects.requireNonNull(permissions, "permissions"));
    }

    public MapBookSnapshot(UUID id, UUID owner, MapBookStatus status, long revision, List<MapBookRegion> regions) {
        this(id, owner, status, revision, regions, Map.of(owner, BookPermission.OWNER));
    }
}

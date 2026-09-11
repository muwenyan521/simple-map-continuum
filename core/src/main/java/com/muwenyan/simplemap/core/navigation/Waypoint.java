package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.Objects;
import java.util.UUID;

public record Waypoint(UUID id, DimensionId dimension, BlockPos position, String name, boolean visible) {
    public Waypoint {
        id = Objects.requireNonNull(id, "id");
        dimension = Objects.requireNonNull(dimension, "dimension");
        position = Objects.requireNonNull(position, "position");
        name = Objects.requireNonNull(name, "name").trim();
        if (name.isEmpty() || name.length() > 128) throw new IllegalArgumentException("invalid name");
    }
    public Waypoint withVisible(boolean value) { return new Waypoint(id, dimension, position, name, value); }
}

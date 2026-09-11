package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.Objects;

public record PlayerMapState(DimensionId dimension, double x, double z, int y, float yaw) {
    public PlayerMapState {
        dimension = Objects.requireNonNull(dimension, "dimension");
        if (!Double.isFinite(x) || !Double.isFinite(z) || !Float.isFinite(yaw)) {
            throw new IllegalArgumentException("invalid player position");
        }
    }

    public BlockPos blockPosition() {
        return new BlockPos((int) Math.floor(x), y, (int) Math.floor(z));
    }
}

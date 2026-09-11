package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.Objects;
import java.util.UUID;

public record MapPin(UUID id, DimensionId dimension, BlockPos position, String label, int colorArgb) {
    public MapPin {
        id = Objects.requireNonNull(id, "id");
        dimension = Objects.requireNonNull(dimension, "dimension");
        position = Objects.requireNonNull(position, "position");
        label = Objects.requireNonNull(label, "label").trim();
        if (label.isEmpty() || label.length() > 128 || (colorArgb >>> 24) == 0) {
            throw new IllegalArgumentException("invalid pin");
        }
    }
}

package com.muwenyan.simplemap.core.map;

import com.muwenyan.simplemap.core.model.BlockPos;
import java.util.Objects;

public record MapCell(BlockPos position, int colorArgb, int height, boolean fluid, boolean complete) {
    public MapCell {
        position = Objects.requireNonNull(position, "position");
    }
}

package com.muwenyan.simplemap.core.minimap;

import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.List;
import java.util.Objects;

public record MinimapFrame(
        DimensionId dimension,
        long generation,
        double playerX,
        double playerZ,
        float playerYaw,
        MinimapConfig config,
        List<MinimapTile> tiles) {
    public MinimapFrame {
        dimension = Objects.requireNonNull(dimension, "dimension");
        config = Objects.requireNonNull(config, "config");
        tiles = List.copyOf(Objects.requireNonNull(tiles, "tiles"));
        if (generation < 0 || !Double.isFinite(playerX) || !Double.isFinite(playerZ)
                || !Float.isFinite(playerYaw)) {
            throw new IllegalArgumentException("invalid minimap frame");
        }
    }
}

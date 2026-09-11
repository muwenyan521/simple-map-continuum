package com.muwenyan.simplemap.core.render;

import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.List;
import java.util.Objects;

public record RenderPlan(DimensionId dimension, long generation, long revision, List<RenderTile> tiles) {
    public RenderPlan {
        dimension = Objects.requireNonNull(dimension, "dimension");
        if (generation < 0 || revision < 0) {
            throw new IllegalArgumentException("invalid render plan version");
        }
        tiles = List.copyOf(Objects.requireNonNull(tiles, "tiles"));
    }

    public MapRenderFrame frame() {
        return new MapRenderFrame(dimension, generation, revision, tiles);
    }
}

package com.muwenyan.simplemap.core.render;

import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.List;
import java.util.Objects;

public record MapRenderFrame(DimensionId dimension, long generation, long frameRevision, List<RenderTile> tiles) {
    public MapRenderFrame {
        dimension = Objects.requireNonNull(dimension, "dimension");
        if (generation < 0 || frameRevision < 0) throw new IllegalArgumentException("invalid frame version");
        tiles = List.copyOf(Objects.requireNonNull(tiles, "tiles"));
    }
}

package com.muwenyan.simplemap.core.render;

import com.muwenyan.simplemap.core.model.TileKey;
import java.util.Objects;

public record RenderTile(TileKey key, long revision, int pixelWidth, int pixelHeight) {
    public RenderTile {
        key = Objects.requireNonNull(key, "key");
        if (revision < 0 || pixelWidth <= 0 || pixelHeight <= 0) throw new IllegalArgumentException("invalid render tile");
    }

    public int lodScale() {
        return 1 << Math.min(30, key.lod());
    }
}

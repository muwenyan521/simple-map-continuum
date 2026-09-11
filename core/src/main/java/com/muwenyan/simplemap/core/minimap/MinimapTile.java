package com.muwenyan.simplemap.core.minimap;

import com.muwenyan.simplemap.core.model.ChunkPos;

public record MinimapTile(ChunkPos chunk, int argb, long revision) {
    public MinimapTile {
        if (revision < 0) {
            throw new IllegalArgumentException("revision must be non-negative");
        }
    }
}

package com.muwenyan.simplemap.core.surface;

import com.muwenyan.simplemap.core.map.MapCell;
import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.Objects;

public record SurfaceSample(ChunkPos chunk, int topY, int floorY, int argb,
                            boolean fluid, boolean complete, long revision) {
    public SurfaceSample {
        chunk = Objects.requireNonNull(chunk, "chunk");
        if ((argb >>> 24) == 0 || floorY > topY || revision < 0) {
            throw new IllegalArgumentException("invalid surface sample");
        }
    }

    public MapCell toCell() {
        return new MapCell(new BlockPos(chunk.x() * 16 + 8, topY, chunk.z() * 16 + 8),
                argb, topY - floorY, fluid, complete);
    }
}

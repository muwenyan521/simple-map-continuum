package com.muwenyan.simplemap.platform.port;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.surface.SurfaceColumn;
import java.util.Optional;

@FunctionalInterface
public interface SurfaceColumnSourcePort {
    Optional<SurfaceColumn> sample(ChunkPos chunk, int localX, int localZ);
}

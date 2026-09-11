package com.muwenyan.simplemap.platform.port;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.Optional;

public interface WorldSourcePort {
    Optional<ChunkSnapshot> snapshot(DimensionId dimension, ChunkPos position);
}

package com.muwenyan.simplemap.platform.port;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.info.WorldInfo;
import com.muwenyan.simplemap.core.model.BlockPos;
import java.util.Optional;

public interface WorldSourcePort {
    Optional<ChunkSnapshot> snapshot(DimensionId dimension, ChunkPos position);

    default WorldInfo inspect(DimensionId dimension, BlockPos position) {
        return WorldInfo.empty(position);
    }
}

package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.RegionPos;
import java.util.Objects;

public final class CoordinateDisplay {
    private CoordinateDisplay() {
    }

    public static String format(BlockPos position, CoordinateFormat format) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(format, "format");
        return switch (format) {
            case BLOCK -> position.x() + ", " + position.y() + ", " + position.z();
            case CHUNK -> formatChunk(new ChunkPos(Math.floorDiv(position.x(), 16), Math.floorDiv(position.z(), 16)));
            case REGION -> formatRegion(RegionPos.fromChunk(new ChunkPos(Math.floorDiv(position.x(), 16), Math.floorDiv(position.z(), 16))));
        };
    }

    private static String formatChunk(ChunkPos position) {
        return position.x() + ", " + position.z();
    }

    private static String formatRegion(RegionPos position) {
        return position.x() + ", " + position.z();
    }
}

package com.muwenyan.simplemap.core.info;

import com.muwenyan.simplemap.core.model.BlockPos;
import java.util.Objects;
import java.util.Optional;

public record WorldInfo(Optional<BlockInfo> block, Optional<BiomeInfo> biome, BlockPos position) {
    public WorldInfo {
        block = Objects.requireNonNull(block, "block");
        biome = Objects.requireNonNull(biome, "biome");
        position = Objects.requireNonNull(position, "position");
    }

    public static WorldInfo empty(BlockPos position) {
        return new WorldInfo(Optional.empty(), Optional.empty(), position);
    }
}

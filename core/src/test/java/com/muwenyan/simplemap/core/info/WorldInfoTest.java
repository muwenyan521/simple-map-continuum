package com.muwenyan.simplemap.core.info;

import com.muwenyan.simplemap.core.model.BlockPos;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldInfoTest {
    @Test
    void keepsOptionalBlockAndBiomeData() {
        BlockInfo block = new BlockInfo("minecraft:stone", "Stone", 0xff777777, false, true);
        BiomeInfo biome = new BiomeInfo("minecraft:plains", "Plains", 0.8f, 0.4f);
        WorldInfo info = new WorldInfo(Optional.of(block), Optional.of(biome), new BlockPos(1, 64, 2));
        assertEquals("Stone", info.block().orElseThrow().displayName());
        assertEquals("Plains", info.biome().orElseThrow().displayName());
    }
}

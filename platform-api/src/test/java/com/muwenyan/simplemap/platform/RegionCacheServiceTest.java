package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionCacheServiceTest {
    @TempDir
    Path temporary;

    @Test
    void writesReadsAndInvalidatesRegionWithoutDelete() throws Exception {
        DimensionId dimension = new DimensionId("minecraft:overworld");
        RegionCacheService cache = new RegionCacheService(temporary);
        MapRegion region = new MapRegion(dimension, new ChunkPos(1, -2), 32, 32);
        cache.write(region);
        assertEquals(region.origin(), cache.read(dimension, 1, -2).orElseThrow().origin());
        assertTrue(cache.invalidate(dimension, 1, -2));
        assertTrue(cache.path(dimension, 1, -2).resolveSibling("r.1.-2.smap.invalid").toFile().isFile());
    }
}

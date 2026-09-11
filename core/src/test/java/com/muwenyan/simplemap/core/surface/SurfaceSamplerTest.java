package com.muwenyan.simplemap.core.surface;

import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurfaceSamplerTest {
    @Test
    void selectsHighestOpaqueColumnAndAppliesToRegion() {
        ChunkPos chunk = new ChunkPos(-1, 2);
        SurfaceSample sample = SurfaceSampler.sample(chunk, List.of(
                new SurfaceColumn(-14, 32, 60, 0xff224466, false, true),
                new SurfaceColumn(-15, 32, 72, 0xff6688aa, true, false),
                new SurfaceColumn(-14, 32, 64, 0xff335577, false, true)), 4).orElseThrow();
        assertEquals(64, sample.topY());
        assertEquals(60, sample.floorY());
        MapRegion region = new MapRegion(new DimensionId("minecraft:overworld"), new ChunkPos(-1, 2), 32, 32);
        assertTrue(SurfaceRegionAssembler.apply(region, sample));
        assertEquals(sample.toCell(), region.cell(0, 0));
        assertFalse(SurfaceRegionAssembler.apply(region, new SurfaceSample(new ChunkPos(31, 2), 1, 1,
                0xff000000, false, true, 1)));
    }
}

package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DimensionRuntimeRegistryTest {
    @Test
    void dimensionRegionsRemainIsolated() {
        DimensionId overworld = new DimensionId("minecraft:overworld");
        DimensionId nether = new DimensionId("minecraft:the_nether");
        DimensionRuntimeRegistry registry = new DimensionRuntimeRegistry();
        registry.put(new MapRegion(overworld, new ChunkPos(0, 0), 32, 32));
        registry.put(new MapRegion(nether, new ChunkPos(0, 0), 32, 32));
        registry.activate(overworld);
        assertEquals(overworld, registry.activeRegion().orElseThrow().dimension());
        assertEquals(2, registry.size());
        assertThrows(IllegalArgumentException.class, () -> registry.activate(new DimensionId("mod:unknown")));
    }
}

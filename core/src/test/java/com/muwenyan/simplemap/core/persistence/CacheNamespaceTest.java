package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.model.ColorMode;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.MapMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheNamespaceTest {
    @Test
    void dimensionsModesAndEpochsProduceDistinctPaths() {
        CacheNamespace surface = new CacheNamespace(new DimensionId("minecraft:overworld"),
                MapMode.SURFACE, ColorMode.ACCURATE, 1);
        CacheNamespace cave = new CacheNamespace(new DimensionId("minecraft:overworld"),
                MapMode.CAVE, ColorMode.VANILLA, 2);
        assertNotEquals(surface.folderName(), cave.folderName());
        assertTrue(surface.regionFile(-1, 2).endsWith("r.-1.2.cache"));
    }
}

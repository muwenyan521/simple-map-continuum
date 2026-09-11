package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.platform.file.RegionFilePersistencePort;
import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.navigation.MapViewport;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegionFilePersistencePortTest {
    @TempDir
    Path temporary;

    @Test
    void adaptsFileCacheToPersistencePort() {
        DimensionId dimension = new DimensionId("minecraft:overworld");
        RegionFilePersistencePort port = new RegionFilePersistencePort(temporary);
        MapRegion region = new MapRegion(dimension, new ChunkPos(0, 0), 32, 32);
        port.writeRegion(region);
        assertEquals(region.origin(), port.readRegion(dimension, 0, 0).orElseThrow().origin());
        MapRuntime runtime = new MapRuntime((ignored, position) -> java.util.Optional.empty(), frame -> { },
                new NavigationState(new MapViewport(0, 0, 1)), MapConfig.defaults());
        assertEquals(true, runtime.loadRegion(port, dimension, 0, 0));
        assertEquals(region.origin(), runtime.currentRegion().origin());
    }
}

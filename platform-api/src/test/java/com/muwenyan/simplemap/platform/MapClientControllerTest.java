package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.model.MapMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapClientControllerTest {
    @Test
    void togglesModeThroughSharedRuntime() {
        MapClientController controller = new MapClientController();
        assertEquals(MapMode.SURFACE, controller.mode());
        assertEquals(MapMode.CAVE, controller.toggleMode());
        assertEquals(MapMode.CAVE, controller.runtime().mode().mode());
    }

    @Test
    void samplesSurfaceThroughCombinedWorldPort() {
        class Source implements com.muwenyan.simplemap.platform.port.WorldSourcePort,
                com.muwenyan.simplemap.platform.port.SurfaceColumnSourcePort {
            @Override
            public java.util.Optional<com.muwenyan.simplemap.core.model.ChunkSnapshot> snapshot(
                    com.muwenyan.simplemap.core.model.DimensionId dimension,
                    com.muwenyan.simplemap.core.model.ChunkPos position) {
                return java.util.Optional.empty();
            }

            @Override
            public java.util.Optional<com.muwenyan.simplemap.core.surface.SurfaceColumn> sample(
                    com.muwenyan.simplemap.core.model.ChunkPos chunk, int x, int z) {
                return java.util.Optional.of(new com.muwenyan.simplemap.core.surface.SurfaceColumn(x, z, 64, 0xFF00FF00, false, true));
            }
        }
        MapClientController controller = new MapClientController(new Source());
        assertEquals(true, controller.refreshSurface(new com.muwenyan.simplemap.core.model.DimensionId("minecraft:overworld"),
                new com.muwenyan.simplemap.core.model.ChunkPos(1, 1), 1));
        assertEquals(9, controller.runtime().currentRegion().completedCells().size());
    }
}

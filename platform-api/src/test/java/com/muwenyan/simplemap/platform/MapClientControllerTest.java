package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.model.MapMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;

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

    @Test
    void executesWaypointCommandThroughController() {
        MapClientController controller = new MapClientController();
        UUID actor = UUID.randomUUID();
        var dimension = new com.muwenyan.simplemap.core.model.DimensionId("minecraft:overworld");
        assertEquals(1, controller.executeWaypointCommand(actor, dimension, "waypoint add home 1 64 2").size());
        assertEquals("home", controller.visibleWaypoints(dimension).get(0).name());
    }

    @Test
    void buildsMinimapFrameFromCurrentRegion() {
        MapClientController controller = new MapClientController(new MapClientControllerTestSource());
        var dimension = new com.muwenyan.simplemap.core.model.DimensionId("minecraft:overworld");
        controller.refreshSurface(dimension, new com.muwenyan.simplemap.core.model.ChunkPos(1, 1), 0);
        var frame = controller.buildMinimap(new com.muwenyan.simplemap.core.navigation.PlayerMapState(dimension, 24, 24, 64, 0),
                com.muwenyan.simplemap.core.minimap.MinimapConfig.defaults(), 1);
        assertEquals(1, frame.tiles().size());
    }

    private static final class MapClientControllerTestSource implements com.muwenyan.simplemap.platform.port.WorldSourcePort,
            com.muwenyan.simplemap.platform.port.SurfaceColumnSourcePort {
        @Override public java.util.Optional<com.muwenyan.simplemap.core.model.ChunkSnapshot> snapshot(
                com.muwenyan.simplemap.core.model.DimensionId dimension, com.muwenyan.simplemap.core.model.ChunkPos position) { return java.util.Optional.empty(); }
        @Override public java.util.Optional<com.muwenyan.simplemap.core.surface.SurfaceColumn> sample(
                com.muwenyan.simplemap.core.model.ChunkPos chunk, int x, int z) {
            return java.util.Optional.of(new com.muwenyan.simplemap.core.surface.SurfaceColumn(x, z, 64, 0xFF00FF00, false, true));
        }
    }
}

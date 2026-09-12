package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.model.MapMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;

class MapClientControllerTest {
    @TempDir java.nio.file.Path temporary;
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

    @Test
    void waypointSyncRoundTripsThroughController() {
        var controller = new MapClientController();
        var dimension = new com.muwenyan.simplemap.core.model.DimensionId("minecraft:overworld");
        controller.executeWaypointCommand(java.util.UUID.randomUUID(), dimension, "waypoint add home 1 2 3");
        var receiver = new MapClientController();
        org.junit.jupiter.api.Assertions.assertEquals(9, receiver.applyWaypointSync(controller.encodeWaypointSync(9)));
        org.junit.jupiter.api.Assertions.assertEquals(1, receiver.visibleWaypoints(dimension).size());
    }

    @Test
    void directWaypointCreationPersistsAndReturnsIdentity() {
        var controller = new MapClientController();
        var dimension = new com.muwenyan.simplemap.core.model.DimensionId("minecraft:overworld");
        var waypoint = controller.addWaypoint(java.util.UUID.randomUUID(), dimension,
                new com.muwenyan.simplemap.core.model.BlockPos(4, 5, 6), "Death");
        org.junit.jupiter.api.Assertions.assertEquals(waypoint, controller.visibleWaypoints(dimension).get(0));
    }

    @Test
    void deathWaypointUsesStableLabel() {
        var controller = new MapClientController();
        var dimension = new com.muwenyan.simplemap.core.model.DimensionId("minecraft:overworld");
        var waypoint = controller.addDeathWaypoint(java.util.UUID.randomUUID(), dimension,
                new com.muwenyan.simplemap.core.model.BlockPos(7, 8, 9));
        org.junit.jupiter.api.Assertions.assertEquals("Death", waypoint.name());
    }

    @Test
    void clearingDimensionRemovesOnlyMatchingWaypoints() {
        var controller = new MapClientController();
        var overworld = new com.muwenyan.simplemap.core.model.DimensionId("minecraft:overworld");
        var nether = new com.muwenyan.simplemap.core.model.DimensionId("minecraft:the_nether");
        controller.addWaypoint(java.util.UUID.randomUUID(), overworld, new com.muwenyan.simplemap.core.model.BlockPos(1, 2, 3), "a");
        controller.addWaypoint(java.util.UUID.randomUUID(), nether, new com.muwenyan.simplemap.core.model.BlockPos(4, 5, 6), "b");
        controller.clearDimension(overworld);
        org.junit.jupiter.api.Assertions.assertEquals(0, controller.visibleWaypoints(overworld).size());
        org.junit.jupiter.api.Assertions.assertEquals(1, controller.visibleWaypoints(nether).size());
    }

    @Test
    void pinCanBeSetAndCleared() {
        var controller = new MapClientController();
        var dimension = new com.muwenyan.simplemap.core.model.DimensionId("minecraft:overworld");
        controller.setPin(dimension, new com.muwenyan.simplemap.core.model.BlockPos(10, 64, 10), "Target");
        org.junit.jupiter.api.Assertions.assertEquals("Target", controller.pin().orElseThrow().label());
        controller.clearPin();
        org.junit.jupiter.api.Assertions.assertTrue(controller.pin().isEmpty());
    }

    @Test
    void minimapPresentationControlsCycleAnchorAndCoordinates() {
        var controller = new MapClientController();
        var initial = controller.minimapConfig();
        controller.cycleMinimapAnchor();
        org.junit.jupiter.api.Assertions.assertNotEquals(initial.anchor(), controller.minimapConfig().anchor());
        boolean shown = controller.minimapConfig().showCoordinates();
        org.junit.jupiter.api.Assertions.assertEquals(!shown, controller.toggleMinimapCoordinates());
    }

    @Test
    void minimapConfigRoundTripsPresentationAndMode() throws Exception {
        java.nio.file.Path config = temporary.resolve("simplemap-minimap.cfg");
        {
            var controller = new MapClientController();
            controller.cycleMinimapAnchor();
            controller.toggleMinimapCoordinates();
            controller.toggleMode();
            controller.saveMinimapConfig(config);
            var restored = new MapClientController();
            restored.loadMinimapConfig(config);
            org.junit.jupiter.api.Assertions.assertEquals(controller.minimapConfig().anchor(), restored.minimapConfig().anchor());
            org.junit.jupiter.api.Assertions.assertEquals(controller.minimapConfig().showCoordinates(), restored.minimapConfig().showCoordinates());
            org.junit.jupiter.api.Assertions.assertEquals(controller.mode(), restored.mode());
        }
    }

    @Test
    void minimapRotationAndShapeControlsChangeConfiguration() {
        var controller = new MapClientController();
        boolean rotation = controller.minimapConfig().rotateWithPlayer();
        controller.toggleMinimapRotation();
        org.junit.jupiter.api.Assertions.assertEquals(!rotation, controller.minimapConfig().rotateWithPlayer());
        var shape = controller.minimapConfig().shape();
        controller.cycleMinimapShape();
        org.junit.jupiter.api.Assertions.assertNotEquals(shape, controller.minimapConfig().shape());
    }

    @Test
    void minimapFrameContainsWaypointMarker() {
        MapClientController controller = new MapClientController(new MapClientControllerTestSource());
        var dimension = new com.muwenyan.simplemap.core.model.DimensionId("minecraft:overworld");
        controller.executeWaypointCommand(java.util.UUID.randomUUID(), dimension, "waypoint add home 16 64 16");
        var frame = controller.buildMinimap(new com.muwenyan.simplemap.core.navigation.PlayerMapState(dimension, 0, 0, 64, 0),
                com.muwenyan.simplemap.core.minimap.MinimapConfig.defaults(), 1);
        org.junit.jupiter.api.Assertions.assertTrue(frame.tiles().stream().anyMatch(tile -> tile.argb() == 0xFFFFD040));
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

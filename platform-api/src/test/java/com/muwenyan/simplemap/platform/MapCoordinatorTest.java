package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.navigation.MapViewport;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import com.muwenyan.simplemap.core.streaming.ChunkDemand;
import com.muwenyan.simplemap.platform.memory.DirectSchedulerPort;
import com.muwenyan.simplemap.platform.port.RenderPort;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapCoordinatorTest {
    @Test
    void refreshPublishesChunkMutationsAndCancellationInvalidatesGeneration() {
        DimensionId dimension = new DimensionId("minecraft:overworld");
        WorldSourcePort world = (ignored, position) -> Optional.of(
                new ChunkSnapshot(dimension, position, 1, new byte[]{1}));
        RenderPort render = frame -> { };
        MapRuntime runtime = new MapRuntime(world, render,
                new NavigationState(new MapViewport(0, 0, 1)), MapConfig.defaults());
        MapCoordinator coordinator = new MapCoordinator(runtime, new DirectSchedulerPort(Runnable::run));
        assertEquals(3, coordinator.refresh(new ChunkDemand(dimension, new ChunkPos(0, 0), 1, 3)).join().size());
        coordinator.cancel();
        assertEquals(1, coordinator.generation());
        assertEquals(1, coordinator.refresh(new ChunkDemand(dimension, new ChunkPos(0, 0), 0, 1)).join().size());
    }
}

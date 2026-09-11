package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.navigation.MapViewport;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import com.muwenyan.simplemap.core.render.MapRenderFrame;
import com.muwenyan.simplemap.core.streaming.ChunkDemand;
import com.muwenyan.simplemap.platform.port.RenderPort;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
import java.util.Optional;

public final class MapRuntimeRefreshTest {
    private MapRuntimeRefreshTest() { }

    public static void main(String[] args) {
        DimensionId dimension = new DimensionId("minecraft:overworld");
        WorldSourcePort world = (ignored, position) -> Optional.of(
                new ChunkSnapshot(dimension, position, 1, new byte[]{(byte) position.x(), (byte) position.z()}));
        RenderPort render = frame -> { };
        MapRuntime runtime = new MapRuntime(world, render,
                new NavigationState(new MapViewport(0, 0, 1)), MapConfig.defaults());
        int count = runtime.refresh(new ChunkDemand(dimension, new ChunkPos(0, 0), 1, 5)).size();
        if (count != 5 || runtime.loadedChunks().size() != 5) {
            throw new AssertionError("runtime refresh");
        }
        System.out.println("MAP_RUNTIME_REFRESH_PASS");
    }
}

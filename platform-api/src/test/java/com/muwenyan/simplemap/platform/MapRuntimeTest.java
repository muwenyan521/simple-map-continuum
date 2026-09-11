package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.navigation.MapViewport;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import com.muwenyan.simplemap.core.render.MapRenderFrame;
import com.muwenyan.simplemap.platform.port.RenderPort;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
import java.util.List;

public final class MapRuntimeTest {
    private MapRuntimeTest() { }
    public static void main(String[] args) {
        final boolean[] published = {false};
        RenderPort render = frame -> published[0] = true;
        WorldSourcePort world = (dimension, position) -> java.util.Optional.empty();
        MapConfig config = MapConfig.defaults();
        MapRuntime runtime = new MapRuntime(world, render, new NavigationState(new MapViewport(0, 0, 1)), config);
        runtime.publish(new MapRenderFrame(new DimensionId("minecraft:overworld"), 1, 1, List.of()));
        if (!published[0] || runtime.world() != world || runtime.config() != config) {
            throw new AssertionError("runtime composition");
        }
        System.out.println("MAP_RUNTIME_COMPOSITION_PASS");
    }
}

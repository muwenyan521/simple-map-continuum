package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.model.MapMode;
import com.muwenyan.simplemap.core.navigation.MapViewport;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import com.muwenyan.simplemap.platform.memory.MemoryRenderPort;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
import com.muwenyan.simplemap.platform.port.SurfaceColumnSourcePort;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.core.map.MapRegion;
import java.util.Objects;

public final class MapClientController {
    private final MapRuntime runtime;

    public MapClientController() {
        this((dimension, position) -> java.util.Optional.empty());
    }

    public MapClientController(WorldSourcePort world) {
        runtime = new MapRuntime(Objects.requireNonNull(world, "world"),
                new MemoryRenderPort(), new NavigationState(new MapViewport(0, 0, 1)),
                MapConfig.defaults());
        runtime.initialize();
    }

    public MapRuntime runtime() { return runtime; }
    public MapMode mode() { return runtime.mode().mode(); }
    public MapMode toggleMode() { return runtime.mode().toggle(); }
    public void setMode(MapMode mode) { runtime.mode().set(Objects.requireNonNull(mode, "mode")); }

    public boolean refreshSurface(DimensionId dimension, ChunkPos center, int radius) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(center, "center");
        if (radius < 0 || radius > 16) throw new IllegalArgumentException("invalid radius");
        RegionPos region = RegionPos.fromChunk(center);
        MapRegion target = runtime.currentRegion();
        if (target == null || !target.dimension().equals(dimension) || !target.origin().equals(new ChunkPos(region.x() << 5, region.z() << 5))) {
            target = new MapRegion(dimension, new ChunkPos(region.x() << 5, region.z() << 5), 32, 32);
            runtime.setRegion(target);
        }
        if (!(runtime.world() instanceof SurfaceColumnSourcePort source)) return false;
        for (int z = center.z() - radius; z <= center.z() + radius; z++) {
            for (int x = center.x() - radius; x <= center.x() + radius; x++) {
                runtime.scanSurface(new ChunkPos(x, z), source, target.revision() + 1);
            }
        }
        return target.revision() > 0;
    }
}

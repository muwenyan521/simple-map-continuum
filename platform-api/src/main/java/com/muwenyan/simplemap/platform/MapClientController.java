package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.model.MapMode;
import com.muwenyan.simplemap.core.navigation.MapViewport;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import com.muwenyan.simplemap.platform.memory.MemoryRenderPort;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
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
}

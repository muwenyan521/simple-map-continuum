package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import com.muwenyan.simplemap.core.navigation.MapViewport;
import com.muwenyan.simplemap.platform.port.RenderPort;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
import java.util.Objects;

public interface PlatformBootstrap {
    PlatformDescriptor descriptor();

    default MapRuntime createRuntime(WorldSourcePort world, RenderPort render, MapConfig config) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(render, "render");
        Objects.requireNonNull(config, "config");
        return new MapRuntime(world, render, new NavigationState(new MapViewport(0, 0, 1)), config);
    }
}

package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import com.muwenyan.simplemap.core.render.MapRenderFrame;
import com.muwenyan.simplemap.platform.port.RenderPort;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
import java.util.Objects;

public final class MapRuntime {
    private final WorldSourcePort world;
    private final RenderPort render;
    private final NavigationState navigation;
    private MapConfig config;
    private MapRegion currentRegion;

    public MapRuntime(WorldSourcePort world, RenderPort render, NavigationState navigation, MapConfig config) {
        this.world = Objects.requireNonNull(world, "world");
        this.render = Objects.requireNonNull(render, "render");
        this.navigation = Objects.requireNonNull(navigation, "navigation");
        this.config = Objects.requireNonNull(config, "config");
    }
    public WorldSourcePort world() { return world; }
    public RenderPort render() { return render; }
    public NavigationState navigation() { return navigation; }
    public MapConfig config() { return config; }
    public void updateConfig(MapConfig next) { config = Objects.requireNonNull(next, "next"); }
    public void publish(MapRenderFrame frame) { render.publish(Objects.requireNonNull(frame, "frame")); }
    public void setRegion(MapRegion region) { currentRegion = Objects.requireNonNull(region, "region"); }
    public MapRegion currentRegion() { return currentRegion; }
}

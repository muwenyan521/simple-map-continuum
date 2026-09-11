package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.config.MapConfigCodec;
import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import com.muwenyan.simplemap.core.render.MapRenderFrame;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.streaming.CenterOutChunkPlanner;
import com.muwenyan.simplemap.core.streaming.ChunkDemand;
import com.muwenyan.simplemap.core.streaming.ChunkMutation;
import com.muwenyan.simplemap.core.streaming.ChunkStore;
import com.muwenyan.simplemap.platform.port.RenderPort;
import com.muwenyan.simplemap.platform.port.ConfigPort;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MapRuntime {
    private final WorldSourcePort world;
    private final RenderPort render;
    private final NavigationState navigation;
    private final ChunkStore chunks;
    private MapConfig config;
    private MapRegion currentRegion;

    public MapRuntime(WorldSourcePort world, RenderPort render, NavigationState navigation, MapConfig config) {
        this.world = Objects.requireNonNull(world, "world");
        this.render = Objects.requireNonNull(render, "render");
        this.navigation = Objects.requireNonNull(navigation, "navigation");
        this.chunks = new ChunkStore(Math.max(64, config.renderDistance() * config.renderDistance() * 4));
        this.config = Objects.requireNonNull(config, "config");
    }
    public WorldSourcePort world() { return world; }
    public RenderPort render() { return render; }
    public NavigationState navigation() { return navigation; }
    public ChunkStore chunks() { return chunks; }
    public MapConfig config() { return config; }
    public void updateConfig(MapConfig next) { config = Objects.requireNonNull(next, "next"); }
    public void loadConfig(ConfigPort port) {
        Objects.requireNonNull(port, "port").read().map(MapConfigCodec::decode).ifPresent(this::updateConfig);
    }
    public void saveConfig(ConfigPort port) {
        Objects.requireNonNull(port, "port").write(MapConfigCodec.encode(config));
    }
    public void publish(MapRenderFrame frame) { render.publish(Objects.requireNonNull(frame, "frame")); }
    public void setRegion(MapRegion region) { currentRegion = Objects.requireNonNull(region, "region"); }
    public MapRegion currentRegion() { return currentRegion; }

    public List<ChunkMutation> refresh(ChunkDemand demand) {
        Objects.requireNonNull(demand, "demand");
        List<ChunkMutation> mutations = new ArrayList<>();
        for (var position : CenterOutChunkPlanner.plan(demand)) {
            world.snapshot(demand.dimension(), position).map(chunks::ingest).ifPresent(mutations::add);
        }
        return List.copyOf(mutations);
    }

    public List<ChunkSnapshot> loadedChunks() {
        return chunks.snapshots();
    }
}

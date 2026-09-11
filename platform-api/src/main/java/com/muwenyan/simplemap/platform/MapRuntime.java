package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.config.MapConfig;
import com.muwenyan.simplemap.core.config.MapConfigCodec;
import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.navigation.NavigationState;
import com.muwenyan.simplemap.core.session.LifecycleState;
import com.muwenyan.simplemap.core.render.MapRenderFrame;
import com.muwenyan.simplemap.core.render.RenderPlan;
import com.muwenyan.simplemap.core.render.RenderPlanner;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.streaming.CenterOutChunkPlanner;
import com.muwenyan.simplemap.core.streaming.ChunkDemand;
import com.muwenyan.simplemap.core.streaming.ChunkMutation;
import com.muwenyan.simplemap.core.streaming.ChunkStore;
import com.muwenyan.simplemap.core.streaming.MutationKind;
import com.muwenyan.simplemap.core.telemetry.MapMetric;
import com.muwenyan.simplemap.core.telemetry.MapTelemetry;
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
    private final DimensionRuntimeRegistry dimensions = new DimensionRuntimeRegistry();
    private final LifecycleState lifecycle = new LifecycleState();
    private final MapTelemetry telemetry = new MapTelemetry();
    private MapConfig config;
    private MapRegion currentRegion;

    public MapRuntime(WorldSourcePort world, RenderPort render, NavigationState navigation, MapConfig config) {
        this.world = Objects.requireNonNull(world, "world");
        this.render = Objects.requireNonNull(render, "render");
        this.navigation = Objects.requireNonNull(navigation, "navigation");
        this.config = Objects.requireNonNull(config, "config");
        this.chunks = new ChunkStore(Math.max(64, config.renderDistance() * config.renderDistance() * 4));
    }
    public WorldSourcePort world() { return world; }
    public RenderPort render() { return render; }
    public NavigationState navigation() { return navigation; }
    public ChunkStore chunks() { return chunks; }
    public DimensionRuntimeRegistry dimensions() { return dimensions; }
    public LifecycleState lifecycle() { return lifecycle; }
    public MapTelemetry telemetry() { return telemetry; }
    public void initialize() { lifecycle.initialize(); }
    public void attachWorld() { lifecycle.attachWorld(); }
    public void detachWorld() { lifecycle.detachWorld(); }
    public void stop() { lifecycle.stop(); }
    public MapConfig config() { return config; }
    public void updateConfig(MapConfig next) { config = Objects.requireNonNull(next, "next"); }
    public void loadConfig(ConfigPort port) {
        Objects.requireNonNull(port, "port").read().map(MapConfigCodec::decode).ifPresent(this::updateConfig);
    }
    public void saveConfig(ConfigPort port) {
        Objects.requireNonNull(port, "port").write(MapConfigCodec.encode(config));
    }
    public void publish(MapRenderFrame frame) { render.publish(Objects.requireNonNull(frame, "frame")); }
    public void setRegion(MapRegion region) {
        currentRegion = Objects.requireNonNull(region, "region");
        dimensions.put(currentRegion);
    }
    public MapRegion currentRegion() { return currentRegion; }

    public void activateDimension(com.muwenyan.simplemap.core.model.DimensionId dimension) {
        dimensions.activate(dimension);
        currentRegion = dimensions.activeRegion().orElseThrow();
    }

    public RenderPlan renderCurrent(int lod, long generation) {
        MapRegion region = Objects.requireNonNull(currentRegion, "current region is not set");
        RenderPlan plan = RenderPlanner.plan(region, lod, generation);
        publish(plan.frame());
        telemetry.add(MapMetric.RENDER_TILES, plan.tiles().size());
        return plan;
    }

    public List<ChunkMutation> refresh(ChunkDemand demand) {
        Objects.requireNonNull(demand, "demand");
        List<ChunkMutation> mutations = new ArrayList<>();
        List<com.muwenyan.simplemap.core.model.ChunkPos> positions = CenterOutChunkPlanner.plan(demand);
        telemetry.add(MapMetric.CHUNKS_REQUESTED, positions.size());
        for (var position : positions) {
            world.snapshot(demand.dimension(), position).map(chunks::ingest).ifPresent(mutations::add);
        }
        telemetry.add(MapMetric.CHUNKS_APPLIED, mutations.stream().filter(m -> m.kind() == MutationKind.APPLIED).count());
        telemetry.add(MapMetric.CHUNKS_STALE, mutations.stream().filter(m -> m.kind() == MutationKind.STALE).count());
        return List.copyOf(mutations);
    }

    public List<ChunkSnapshot> loadedChunks() {
        return chunks.snapshots();
    }
}

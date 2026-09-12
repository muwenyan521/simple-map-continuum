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
import com.muwenyan.simplemap.core.navigation.Waypoint;
import com.muwenyan.simplemap.core.waypoint.WaypointCommandExecutor;
import com.muwenyan.simplemap.core.waypoint.WaypointCommandParser;
import com.muwenyan.simplemap.core.waypoint.WaypointStore;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import com.muwenyan.simplemap.core.navigation.PlayerMapState;
import com.muwenyan.simplemap.core.minimap.MinimapConfig;
import com.muwenyan.simplemap.core.minimap.MinimapFrame;
import com.muwenyan.simplemap.core.minimap.MinimapFrameBuilder;
import com.muwenyan.simplemap.core.minimap.MinimapTile;
import com.muwenyan.simplemap.platform.file.FileConfigPort;
import com.muwenyan.simplemap.platform.file.WaypointFileService;
import com.muwenyan.simplemap.platform.port.CaveColumnSourcePort;
import com.muwenyan.simplemap.core.cave.CaveConfig;
import com.muwenyan.simplemap.core.cave.CaveSnapshot;
import java.util.Map;
import java.util.LinkedHashMap;

public final class MapClientController {
    private final MapRuntime runtime;
    private final WaypointStore waypoints = new WaypointStore();
    private MapBookRuntime books;
    private WaypointFileService waypointFiles;
    private java.nio.file.Path waypointRoot;
    private boolean minimapEnabled = true;
    private final Map<ChunkPos, CaveSnapshot> caveSnapshots = new LinkedHashMap<>();

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
    public synchronized void bindBookStorage(java.nio.file.Path root) { books = new MapBookRuntime(root); }
    public synchronized MapBookRuntime books() { return Objects.requireNonNull(books, "book storage is not bound"); }
    public synchronized void loadConfig(java.nio.file.Path path) { runtime.loadConfig(new FileConfigPort(path)); }
    public synchronized void saveConfig(java.nio.file.Path path) { runtime.saveConfig(new FileConfigPort(path)); }
    public synchronized void bindWaypointStorage(java.nio.file.Path root) {
        java.nio.file.Path normalized = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
        if (normalized.equals(waypointRoot)) return;
        waypoints.clear();
        waypointRoot = normalized;
        waypointFiles = new WaypointFileService(normalized);
        try { waypointFiles.readInto(waypoints); }
        catch (java.io.IOException exception) { throw new IllegalStateException("cannot load waypoints", exception); }
    }
    public synchronized void saveWaypoints() {
        if (waypointFiles == null) throw new IllegalStateException("waypoint storage is not bound");
        try { waypointFiles.write(waypoints); }
        catch (java.io.IOException exception) { throw new IllegalStateException("cannot save waypoints", exception); }
    }
    public MapMode mode() { return runtime.mode().mode(); }
    public MapMode toggleMode() { return runtime.mode().toggle(); }
    public synchronized boolean minimapEnabled() { return minimapEnabled; }
    public synchronized boolean toggleMinimap() { minimapEnabled = !minimapEnabled; return minimapEnabled; }
    public void setMode(MapMode mode) { runtime.mode().set(Objects.requireNonNull(mode, "mode")); }
    public List<Waypoint> executeWaypointCommand(UUID actor, DimensionId dimension, String command) {
        List<Waypoint> result = new WaypointCommandExecutor(waypoints).execute(Objects.requireNonNull(actor, "actor"),
                Objects.requireNonNull(dimension, "dimension"), WaypointCommandParser.parse(command));
        if (waypointFiles != null) saveWaypoints();
        return result;
    }
    public List<Waypoint> visibleWaypoints(DimensionId dimension) { return waypoints.visible(Objects.requireNonNull(dimension, "dimension")); }

    public MinimapFrame buildMinimap(PlayerMapState player, MinimapConfig config, long generation) {
        Objects.requireNonNull(player, "player");
        MapRegion region = runtime.currentRegion();
        java.util.ArrayList<MinimapTile> tiles = new java.util.ArrayList<>();
        if (region != null && region.dimension().equals(player.dimension())) {
            for (int z = 0; z < 32; z++) for (int x = 0; x < 32; x++) {
                var cell = region.cell(x, z);
                if (cell != null) tiles.add(new MinimapTile(new ChunkPos(region.origin().x() + x, region.origin().z() + z), cell.colorArgb(), region.revision()));
            }
        }
        return MinimapFrameBuilder.build(player, Objects.requireNonNull(config, "config"), generation, tiles);
    }

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

    public synchronized int refreshCave(DimensionId dimension, ChunkPos center, int radius, CaveConfig caveConfig) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(center, "center");
        Objects.requireNonNull(caveConfig, "caveConfig");
        if (radius < 0 || radius > 8) throw new IllegalArgumentException("invalid radius");
        if (!(runtime.world() instanceof CaveColumnSourcePort source)) return 0;
        int scanned = 0;
        for (int z = center.z() - radius; z <= center.z() + radius; z++) {
            for (int x = center.x() - radius; x <= center.x() + radius; x++) {
                runtime.scanCave(new ChunkPos(x, z), source, caveConfig, caveSnapshots.size() + 1)
                        .ifPresent(snapshot -> { caveSnapshots.put(snapshot.chunk(), snapshot); });
                scanned++;
            }
        }
        return scanned;
    }

    public synchronized Map<ChunkPos, CaveSnapshot> caveSnapshots() { return Map.copyOf(caveSnapshots); }
}

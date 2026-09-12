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
import com.muwenyan.simplemap.core.protocol.WaypointSyncCodec;
import com.muwenyan.simplemap.core.protocol.WaypointSyncMessage;
import com.muwenyan.simplemap.core.protocol.ProtocolException;
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
import com.muwenyan.simplemap.platform.file.CaveRegionFileService;
import com.muwenyan.simplemap.core.persistence.CaveRegionArchive;
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
    private CaveRegionFileService caveFiles;
    private java.nio.file.Path caveRoot;
    private java.nio.file.Path waypointRoot;
    private boolean minimapEnabled = true;
    private MinimapConfig minimapConfig = MinimapConfig.defaults();
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
    public synchronized void bindCaveStorage(java.nio.file.Path root) {
        java.nio.file.Path normalized = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
        if (!normalized.equals(caveRoot)) {
            caveRoot = normalized;
            caveFiles = new CaveRegionFileService(normalized);
        }
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
    public synchronized MinimapConfig minimapConfig() { return minimapConfig; }
    public synchronized MinimapConfig zoomMinimap(double factor) {
        if (!Double.isFinite(factor) || factor <= 0) throw new IllegalArgumentException("invalid zoom factor");
        double zoom = Math.max(0.125d, Math.min(64d, minimapConfig.zoom() * factor));
        minimapConfig = new MinimapConfig(minimapConfig.enabled(), minimapConfig.sizePixels(), zoom,
                minimapConfig.shape(), minimapConfig.anchor(), minimapConfig.rotateWithPlayer(), minimapConfig.showCoordinates());
        return minimapConfig;
    }
    public synchronized void loadMinimapConfig(java.nio.file.Path path) {
        java.util.Optional<String> encoded = new FileConfigPort(path).read();
        if (encoded.isEmpty()) return;
        try {
            double zoom = Double.parseDouble(encoded.get().trim());
            if (Double.isFinite(zoom) && zoom >= 0.125d && zoom <= 64d) {
                minimapConfig = new MinimapConfig(minimapConfig.enabled(), minimapConfig.sizePixels(), zoom,
                        minimapConfig.shape(), minimapConfig.anchor(), minimapConfig.rotateWithPlayer(), minimapConfig.showCoordinates());
            }
        } catch (NumberFormatException ignored) { }
    }
    public synchronized void saveMinimapConfig(java.nio.file.Path path) {
        new FileConfigPort(path).write(Double.toString(minimapConfig.zoom()));
    }
    public synchronized boolean cycleMinimapAnchor() {
        var anchors = com.muwenyan.simplemap.core.minimap.MinimapAnchor.values();
        int next = (minimapConfig.anchor().ordinal() + 1) % anchors.length;
        minimapConfig = new MinimapConfig(minimapConfig.enabled(), minimapConfig.sizePixels(), minimapConfig.zoom(),
                minimapConfig.shape(), anchors[next], minimapConfig.rotateWithPlayer(), minimapConfig.showCoordinates());
        return true;
    }
    public synchronized boolean toggleMinimapCoordinates() {
        minimapConfig = new MinimapConfig(minimapConfig.enabled(), minimapConfig.sizePixels(), minimapConfig.zoom(),
                minimapConfig.shape(), minimapConfig.anchor(), minimapConfig.rotateWithPlayer(), !minimapConfig.showCoordinates());
        return minimapConfig.showCoordinates();
    }
    public void setMode(MapMode mode) { runtime.mode().set(Objects.requireNonNull(mode, "mode")); }
    public List<Waypoint> executeWaypointCommand(UUID actor, DimensionId dimension, String command) {
        List<Waypoint> result = new WaypointCommandExecutor(waypoints).execute(Objects.requireNonNull(actor, "actor"),
                Objects.requireNonNull(dimension, "dimension"), WaypointCommandParser.parse(command));
        if (waypointFiles != null) saveWaypoints();
        return result;
    }
    public List<Waypoint> visibleWaypoints(DimensionId dimension) { return waypoints.visible(Objects.requireNonNull(dimension, "dimension")); }
    public synchronized java.util.Optional<Waypoint> followedWaypoint() { return waypoints.followed(); }
    public synchronized byte[] encodeWaypointSync(long revision) {
        try { return WaypointSyncCodec.encode(new WaypointSyncMessage(revision, waypoints.all())); }
        catch (ProtocolException exception) { throw new IllegalStateException("cannot encode waypoint sync", exception); }
    }
    public synchronized long applyWaypointSync(byte[] payload) {
        try {
            var message = WaypointSyncCodec.decode(payload);
            waypoints.replaceAll(message.waypoints());
            return message.revision();
        } catch (ProtocolException exception) { throw new IllegalArgumentException("invalid waypoint sync", exception); }
    }

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
        if (mode() == MapMode.CAVE) {
            for (var entry : caveSnapshots.entrySet()) {
                var runs = entry.getValue().columns().stream().flatMap(List::stream).findFirst();
                if (runs.isPresent()) tiles.add(new MinimapTile(entry.getKey(), runs.get().argb(), entry.getValue().revision()));
            }
        }
        for (Waypoint waypoint : visibleWaypoints(player.dimension())) {
            int color = followedWaypoint().map(value -> value.id().equals(waypoint.id())).orElse(false)
                    ? 0xFFFF4040 : 0xFFFFD040;
            tiles.add(new MinimapTile(new ChunkPos(Math.floorDiv(waypoint.position().x(), 16),
                    Math.floorDiv(waypoint.position().z(), 16)), color, Long.MAX_VALUE));
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
                        .ifPresent(snapshot -> {
                            caveSnapshots.put(snapshot.chunk(), snapshot);
                            if (caveFiles != null) persistCaveSnapshot(dimension, snapshot, caveConfig);
                        });
                scanned++;
            }
        }
        return scanned;
    }

    public synchronized Map<ChunkPos, CaveSnapshot> caveSnapshots() { return Map.copyOf(caveSnapshots); }

    public synchronized int restoreCave(DimensionId dimension, int epoch, int regionX, int regionZ) {
        if (caveFiles == null) throw new IllegalStateException("cave storage is not bound");
        Objects.requireNonNull(dimension, "dimension");
        try {
            CaveRegionFileService files = new CaveRegionFileService(caveRoot.resolve(
                    dimension.value().replace(':', '_').replace('/', '_')));
            Map<ChunkPos, CaveSnapshot> restored = files.read(epoch, mode().ordinal(), regionX, regionZ);
            caveSnapshots.putAll(restored);
            return restored.size();
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("cannot restore cave snapshots", exception);
        }
    }

    private void persistCaveSnapshot(DimensionId dimension, CaveSnapshot snapshot, CaveConfig config) {
        try {
            int regionX = Math.floorDiv(snapshot.chunk().x(), 32);
            int regionZ = Math.floorDiv(snapshot.chunk().z(), 32);
            CaveRegionFileService dimensionFiles = new CaveRegionFileService(caveRoot.resolve(
                    dimension.value().replace(':', '_').replace('/', '_')));
            Map<ChunkPos, CaveSnapshot> region = new LinkedHashMap<>();
            region.putAll(dimensionFiles.read(0, mode().ordinal(), regionX, regionZ));
            region.put(snapshot.chunk(), snapshot);
            dimensionFiles.write(0, mode().ordinal(), regionX, regionZ, region);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("cannot persist cave snapshot", exception);
        }
    }
}

package com.muwenyan.simplemap.platform.file;

import com.muwenyan.simplemap.core.navigation.Waypoint;
import com.muwenyan.simplemap.core.waypoint.WaypointCodec;
import com.muwenyan.simplemap.core.waypoint.WaypointStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class WaypointFileService {
    private final Path path;

    public WaypointFileService(Path root) { path = Objects.requireNonNull(root, "root").toAbsolutePath().normalize().resolve("simplemap_waypoints.smpw"); }

    public void write(WaypointStore store) throws IOException {
        Objects.requireNonNull(store, "store");
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
        boolean moved = false;
        try {
            Files.write(temporary, WaypointCodec.encode(store.all()));
            WaypointCodec.decode(Files.readAllBytes(temporary));
            if (Files.exists(path)) Files.copy(path, path.resolveSibling(path.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException exception) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
            moved = true;
        } finally { if (!moved) Files.deleteIfExists(temporary); }
    }

    public void readInto(WaypointStore store) throws IOException {
        Objects.requireNonNull(store, "store");
        if (!Files.isRegularFile(path)) return;
        for (Waypoint waypoint : WaypointCodec.decode(Files.readAllBytes(path))) store.upsert(waypoint);
    }

    public Path path() { return path; }
}

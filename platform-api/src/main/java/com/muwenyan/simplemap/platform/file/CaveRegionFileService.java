package com.muwenyan.simplemap.platform.file;

import com.muwenyan.simplemap.core.cave.CaveSnapshot;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.persistence.ArchiveException;
import com.muwenyan.simplemap.core.persistence.CaveRegionArchive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;

public final class CaveRegionFileService {
    private final Path root;

    public CaveRegionFileService(Path root) {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
    }

    public Path path(int epoch, int mode, int regionX, int regionZ) {
        if (epoch < 0 || mode < 0 || mode > 255) throw new IllegalArgumentException("invalid cave namespace");
        return root.resolve("c" + epoch + "-m" + mode).resolve("r." + regionX + "." + regionZ + ".cvr");
    }

    public void write(int epoch, int mode, int regionX, int regionZ,
                      Map<ChunkPos, CaveSnapshot> snapshots) throws IOException {
        Path target = path(epoch, mode, regionX, regionZ);
        Files.createDirectories(target.getParent());
        if (Files.exists(target)) {
            Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        }
        byte[] encoded = CaveRegionArchive.encode(snapshots);
        Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        boolean moved = false;
        try {
            Files.write(temporary, encoded);
            CaveRegionArchive.decode(Files.readAllBytes(temporary));
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) Files.deleteIfExists(temporary);
        }
    }

    public Map<ChunkPos, CaveSnapshot> read(int epoch, int mode, int regionX, int regionZ) throws IOException {
        Path target = path(epoch, mode, regionX, regionZ);
        if (!Files.isRegularFile(target)) return Map.of();
        try {
            return CaveRegionArchive.decode(Files.readAllBytes(target));
        } catch (ArchiveException exception) {
            throw new IOException("invalid CVR archive", exception);
        }
    }
}

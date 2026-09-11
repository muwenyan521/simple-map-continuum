package com.muwenyan.simplemap.core.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class SurfaceArchiveMigration {
    private SurfaceArchiveMigration() { }

    public static SurfaceRegionArchive readAndUpgrade(Path source) throws IOException {
        SurfaceRegionArchive archive = SurfaceRegionArchive.decode(Files.readAllBytes(source));
        if (archive.version() == 6) return archive;
        return new SurfaceRegionArchive(archive.pixels(), archive.tints(), archive.biomes(), archive.blocks(),
                archive.completeChunks(), 6);
    }

    public static void upgradeInPlace(Path source) throws IOException {
        SurfaceRegionArchive upgraded = readAndUpgrade(source);
        Path backup = source.resolveSibling(source.getFileName() + ".bak");
        Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
        Path temporary = Files.createTempFile(source.toAbsolutePath().getParent(), source.getFileName().toString(), ".tmp");
        boolean moved = false;
        try {
            Files.write(temporary, upgraded.encode());
            SurfaceRegionArchive.decode(Files.readAllBytes(temporary));
            Files.move(temporary, source, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            moved = true;
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(temporary, source, StandardCopyOption.REPLACE_EXISTING);
            moved = true;
        } finally {
            if (!moved) Files.deleteIfExists(temporary);
        }
    }
}

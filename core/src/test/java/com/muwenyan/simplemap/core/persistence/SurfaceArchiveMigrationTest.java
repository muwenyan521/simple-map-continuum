package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.surface.PackedSurfaceCell;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurfaceArchiveMigrationTest {
    @TempDir Path temporary;

    @Test
    void upgradesLegacyArchiveAndLeavesBackup() throws Exception {
        long[] pixels = new long[SurfaceRegionArchive.PIXELS];
        Arrays.fill(pixels, PackedSurfaceCell.EMPTY);
        SurfaceRegionArchive legacy = new SurfaceRegionArchive(pixels, new int[pixels.length],
                new String[0], new String[0], new long[SurfaceRegionArchive.COVERAGE_WORDS], 1);
        Path source = temporary.resolve("region.smdat");
        Files.write(source, legacy.encode());
        SurfaceArchiveMigration.upgradeInPlace(source);
        assertEquals(6, SurfaceRegionArchive.decode(Files.readAllBytes(source)).version());
        assertTrue(Files.exists(source.resolveSibling("region.smdat.bak")));
    }
}

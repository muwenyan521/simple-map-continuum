package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.model.CaveTile;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.core.model.TileKey;
import com.muwenyan.simplemap.platform.file.CaveTileFilePersistencePort;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CaveTileFilePersistencePortTest {
    @TempDir
    Path temporary;

    @Test
    void writesAndReadsCaveTile() {
        TileKey key = new TileKey(new DimensionId("minecraft:the_nether"), new RegionPos(0, 0), 1, 2, 3);
        CaveTileFilePersistencePort port = new CaveTileFilePersistencePort(temporary);
        port.write(new CaveTile(key, 8, new byte[]{1, 2}));
        CaveTile loaded = port.read(8, key).orElseThrow();
        assertArrayEquals(new byte[]{1, 2}, loaded.pixels());
        assertEquals(temporary.resolve("minecraft_the_nether/e8/m1/t.2.3.cvr"), port.path(8, key));
    }

    @Test
    void doesNotReadTileFromAnotherEpochNamespace() {
        TileKey key = new TileKey(new DimensionId("minecraft:overworld"), new RegionPos(0, 0), 0, 0, 0);
        CaveTileFilePersistencePort port = new CaveTileFilePersistencePort(temporary);
        port.write(new CaveTile(key, 7, new byte[]{7}));

        assertEquals(1, port.read(7, key).orElseThrow().pixels().length);
        assertEquals(java.util.Optional.empty(), port.read(8, key));
    }
}

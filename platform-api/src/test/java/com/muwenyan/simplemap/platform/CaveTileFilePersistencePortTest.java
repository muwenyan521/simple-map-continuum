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

class CaveTileFilePersistencePortTest {
    @TempDir
    Path temporary;

    @Test
    void writesAndReadsCaveTile() {
        TileKey key = new TileKey(new DimensionId("minecraft:the_nether"), new RegionPos(0, 0), 1, 2, 3);
        CaveTileFilePersistencePort port = new CaveTileFilePersistencePort(temporary);
        port.write(new CaveTile(key, 8, new byte[]{1, 2}));
        CaveTile loaded = port.read(key).orElseThrow();
        assertArrayEquals(new byte[]{1, 2}, loaded.pixels());
    }
}

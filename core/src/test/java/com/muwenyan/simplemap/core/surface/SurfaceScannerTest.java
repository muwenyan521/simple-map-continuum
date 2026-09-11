package com.muwenyan.simplemap.core.surface;

import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurfaceScannerTest {
    @Test
    void scansAllColumnsAndReturnsHighestSurface() {
        ColumnSource source = (x, z) -> Optional.of(new SurfaceColumn(x, z, x + z,
                0xff202020, false, true));
        SurfaceSample sample = SurfaceScanner.scan(new ChunkPos(2, -3), source, 2).orElseThrow();
        assertEquals(30, sample.topY());
        assertEquals(30, sample.floorY());
    }
}

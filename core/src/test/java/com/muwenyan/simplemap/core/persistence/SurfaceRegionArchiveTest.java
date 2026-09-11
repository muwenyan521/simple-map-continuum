package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.surface.PackedSurfaceCell;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SurfaceRegionArchiveTest {
    @Test
    void roundTripsLegacyV1AndCurrentV6Layouts() throws Exception {
        long[] pixels = new long[SurfaceRegionArchive.PIXELS];
        int[] tints = new int[SurfaceRegionArchive.PIXELS];
        Arrays.fill(pixels, PackedSurfaceCell.pack((short) 70, (short) 1, (byte) 0, (byte) 0x20, (short) 60));
        Arrays.fill(tints, 0xff112233);
        String[] biomes = {"minecraft:plains"}; String[] blocks = {"minecraft:stone"};
        long[] coverage = new long[SurfaceRegionArchive.COVERAGE_WORDS]; coverage[0] = 3;
        SurfaceRegionArchive current = new SurfaceRegionArchive(pixels, tints, biomes, blocks, coverage, 6);
        SurfaceRegionArchive decoded = SurfaceRegionArchive.decode(current.encode());
        assertEquals(6, decoded.version()); assertArrayEquals(pixels, decoded.pixels()); assertArrayEquals(tints, decoded.tints());
        SurfaceRegionArchive legacy = new SurfaceRegionArchive(pixels, tints, biomes, blocks, coverage, 1);
        assertEquals(1, SurfaceRegionArchive.decode(legacy.encode()).version());
    }
}

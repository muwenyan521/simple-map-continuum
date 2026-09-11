package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.model.CaveTile;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.core.model.TileKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CaveTileCodecTest {
    @Test
    void roundTripPreservesKeyEpochAndPixels() throws Exception {
        CaveTile tile = new CaveTile(new TileKey(new DimensionId("minecraft:the_nether"),
                new RegionPos(-1, 2), 3, 4, 5), 8, new byte[]{1, 2, 3});
        CaveTile decoded = CaveTileCodec.decode(CaveTileCodec.encode(tile));
        assertEquals(tile.key(), decoded.key());
        assertEquals(tile.epoch(), decoded.epoch());
        assertArrayEquals(tile.pixels(), decoded.pixels());
    }
}

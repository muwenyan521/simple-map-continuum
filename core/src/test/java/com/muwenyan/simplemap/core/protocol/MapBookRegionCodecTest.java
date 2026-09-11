package com.muwenyan.simplemap.core.protocol;

import com.muwenyan.simplemap.core.book.MapBookRegion;
import com.muwenyan.simplemap.core.model.RegionPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MapBookRegionCodecTest {
    @Test
    void roundTripAndCrcValidation() throws Exception {
        MapBookRegion region = new MapBookRegion(new RegionPos(-2, 4), 7, new byte[]{1, 2, 3});
        MapBookRegion decoded = MapBookRegionCodec.decode(MapBookRegionCodec.encode(region));
        assertEquals(region.position(), decoded.position());
        assertEquals(region.revision(), decoded.revision());
        assertArrayEquals(region.payload(), decoded.payload());
        byte[] corrupt = MapBookRegionCodec.encode(region);
        corrupt[corrupt.length - 5] ^= 1;
        assertThrows(ProtocolException.class, () -> MapBookRegionCodec.decode(corrupt));
    }
}
